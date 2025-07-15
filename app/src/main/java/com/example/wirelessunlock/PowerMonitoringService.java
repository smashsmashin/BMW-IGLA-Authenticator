package com.example.wirelessunlock;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.app.KeyguardManager;


public class PowerMonitoringService extends Service {

    private static final String TAG = "PowerMonitoringService";
    private static final String CHANNEL_ID = "PowerMonitoringChannel";
    private static final int NOTIFICATION_ID = 1;

    private BroadcastReceiver powerBroadcastReceiver;
    private boolean isWirelessChargingServiceScope = false; // Service specific tracking
    private boolean activityLaunched = false; // Add this flag

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: Service creating.");
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());

        registerPowerReceiver();
        Log.d(TAG, "onCreate: Power receiver registered.");
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, DummyLauncherActivity.class); // Open app on tap
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Wireless Unlock Active")
                .setContentText("Monitoring wireless charging status.")
                .setSmallIcon(R.drawable.ic_service_notification) // Use dedicated notification icon
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Power Monitoring Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
                Log.d(TAG, "Notification channel created.");
            } else {
                Log.e(TAG, "NotificationManager is null, cannot create channel.");
            }
        }
    }

    private void registerPowerReceiver() {
        powerBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(TAG, "Programmatic Receiver: Received action: " + action);

                KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);

                if (Intent.ACTION_BATTERY_CHANGED.equals(action) || Intent.ACTION_POWER_CONNECTED.equals(action)) {
                    int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                    boolean previouslyWirelessCharging = isWirelessChargingServiceScope;
                    isWirelessChargingServiceScope = (chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS);

                    if (isWirelessChargingServiceScope != previouslyWirelessCharging || Intent.ACTION_POWER_CONNECTED.equals(action)) {
                        Log.d(TAG, "Programmatic Receiver: Wireless charging: " + isWirelessChargingServiceScope);
                    }

                    if (isWirelessChargingServiceScope) {
                        if (keyguardManager != null && !keyguardManager.isDeviceLocked()) {
                            // Device is unlocked and wireless charging is active.
                            if (!activityLaunched) {
                                Log.d(TAG, "Programmatic Receiver: Device unlocked and wireless charging. Triggering action.");
                                triggerFlashlightActivity(context);
                                activityLaunched = true; // Mark as launched
                            } else {
                                Log.d(TAG, "Programmatic Receiver: Action already triggered for this charging session.");
                            }
                        } else {
                            Log.d(TAG, "Programmatic Receiver: Device is locked. Waiting for unlock.");
                        }
                    }
                } else if (Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
                    isWirelessChargingServiceScope = false;
                    activityLaunched = false; // Reset the flag
                    Log.d(TAG, "Programmatic Receiver: Power disconnected. Resetting state.");
                } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                    Log.d(TAG, "Programmatic Receiver: Device unlocked by user.");
                    // Check charging status again on unlock to be sure
                    IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                    Intent batteryStatus = context.registerReceiver(null, filter);
                    if (batteryStatus != null) {
                        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                        isWirelessChargingServiceScope = (chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS);
                    }

                    if (isWirelessChargingServiceScope && !activityLaunched) {
                        Log.d(TAG, "Programmatic Receiver: Device unlocked while wireless charging. Triggering action.");
                        triggerFlashlightActivity(context);
                        activityLaunched = true; // Mark as launched
                    } else if (isWirelessChargingServiceScope) {
                        Log.d(TAG, "Programmatic Receiver: Device unlocked, but action already triggered for this session.");
                    } else {
                        Log.d(TAG, "Programmatic Receiver: Device unlocked but not on wireless charge.");
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_USER_PRESENT); // Also listen for unlock events here
        // Note: We are not adding the other diagnostic intents (TIMEZONE, SCREEN_ON/OFF, etc.) here
        // as this receiver is specifically for the core power logic.
        // The manifest receiver can continue to handle those if needed for diagnostics.
        registerReceiver(powerBroadcastReceiver, filter);
    }

    private void triggerFlashlightActivity(Context context) {
        if (!isAccessibilityServiceEnabled(context)) {
            promptToEnableAccessibilityService(context);
            return;
        }

        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.dma.author.authorid", "com.dma.author.authorid.view.SplashActivity"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private boolean isAccessibilityServiceEnabled(Context context) {
        ComponentName expectedComponentName = new ComponentName(context, MyAccessibilityService.class);
        String enabledServicesSetting = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabledServicesSetting == null) {
            return false;
        }
        return enabledServicesSetting.contains(expectedComponentName.flattenToString());
    }

    private void promptToEnableAccessibilityService(Context context) {
        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: Service started.");
        // If killed, service will restart.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Service destroying.");
        if (powerBroadcastReceiver != null) {
            unregisterReceiver(powerBroadcastReceiver);
            Log.d(TAG, "onDestroy: Power receiver unregistered.");
        }
        // Consider stopping foreground and removing notification if appropriate
        // stopForeground(true); // For true, removes notification. For false, detaches but notification might remain.
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not a bound service
    }
}
