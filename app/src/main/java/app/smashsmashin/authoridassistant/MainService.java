package app.smashsmashin.authoridassistant;

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
import android.os.Handler;
import android.os.Looper;


public class MainService extends Service {

    private static final String TAG = "MainService";
    private static final String CHANNEL_ID = "AuthorIDAssistantChannel";
    private static final int NOTIFICATION_ID = 1;

    private BroadcastReceiver powerBroadcastReceiver;
    private boolean isWirelessCharging = false;
    private boolean isScreenUnlocked = false;
    private boolean isKeyFobActivated = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate: Service creating.");
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());

        registerPowerReceiver();
        Log.d(LOG_TAG, "onCreate: Power receiver registered.");
    }

    private static final String LOG_TAG = "AuthorIDAssistant";

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, LauncherActivity.class); // Open app on tap
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Author ID Assistant")
                .setContentText("Monitoring device status.")
                .setSmallIcon(R.drawable.ic_notification) // Use dedicated notification icon
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Author ID Assistant Service Channel",
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
                Log.d(LOG_TAG, "Received action: " + action);

                KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                isScreenUnlocked = keyguardManager != null && !keyguardManager.isDeviceLocked();

                if (Intent.ACTION_BATTERY_CHANGED.equals(action) || Intent.ACTION_POWER_CONNECTED.equals(action)) {
                    int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                    boolean previouslyWirelessCharging = isWirelessCharging;
                    isWirelessCharging = (chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS);

                    if (isWirelessCharging != previouslyWirelessCharging) {
                        Log.d(LOG_TAG, "Wireless charging state changed to: " + isWirelessCharging);
                        if (!isWirelessCharging) {
                            deactivateKeyFob();
                        }
                    }

                    if (isWirelessCharging) {
                        if (isScreenUnlocked && !isKeyFobActivated) {
                            activateKeyFob();
                        }
                    }
                } else if (Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
                    Log.d(LOG_TAG, "Power disconnected.");
                    isWirelessCharging = false;
                    deactivateKeyFob();
                } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                    Log.d(LOG_TAG, "Device unlocked by user.");
                    isScreenUnlocked = true;
                    if (isWirelessCharging && !isKeyFobActivated) {
                        activateKeyFob();
                    }
                } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                    Log.d(LOG_TAG, "Screen off.");
                    isScreenUnlocked = false;
                    deactivateKeyFob();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(powerBroadcastReceiver, filter);
    }

    private Handler deactivationHandler = new Handler(Looper.getMainLooper());
    private Runnable deactivationRunnable = () -> deactivateKeyFob();

    private void activateKeyFob() {
        Log.d(LOG_TAG, "Attempting to activate Key FOB.");
        if (!isWirelessCharging || !isScreenUnlocked || isKeyFobActivated) {
            return;
        }
        isKeyFobActivated = true;
        AppState.isKeyFobActionPending = true;
        AppState.shouldActivate = true;
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.dma.author.authorid", "com.dma.author.authorid.view.SplashActivity"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            // Schedule deactivation after 1 minute
            deactivationHandler.postDelayed(deactivationRunnable, 60000);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Author ID app not found.");
        }
    }

    private void deactivateKeyFob() {
        Log.d(LOG_TAG, "Attempting to deactivate Key FOB.");
        deactivationHandler.removeCallbacks(deactivationRunnable); // Remove any pending deactivation
        isKeyFobActivated = false;
        AppState.isKeyFobActionPending = true;
        AppState.shouldActivate = false;
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.dma.author.authorid", "com.dma.author.authorid.view.SplashActivity"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Author ID app not found.");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand: Service started.");
        // If killed, service will restart.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy: Service destroying.");
        if (powerBroadcastReceiver != null) {
            unregisterReceiver(powerBroadcastReceiver);
            Log.d(LOG_TAG, "onDestroy: Power receiver unregistered.");
        }
        // Consider stopping foreground and removing notification if appropriate
        // stopForeground(true); // For true, removes notification. For false, detaches but notification might remain.
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not a bound service
    }
}
