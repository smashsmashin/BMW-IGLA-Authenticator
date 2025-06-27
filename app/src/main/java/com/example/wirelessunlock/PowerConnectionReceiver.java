package com.example.wirelessunlock;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class PowerConnectionReceiver extends BroadcastReceiver {

    private static final String TAG = "PowerConnectionReceiver";
    private boolean isWirelessCharging = false;
    // private Context mContext; // Removed mContext field

    @Override
    public void onReceive(Context context, Intent intent) {
        // mContext = context; // Removed
        String action = intent.getAction();
        Log.d(TAG, "Received action: " + action);

        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);

        if (Intent.ACTION_BATTERY_CHANGED.equals(action) || Intent.ACTION_POWER_CONNECTED.equals(action)) {
            int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean previouslyWirelessCharging = isWirelessCharging;
            isWirelessCharging = (chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS);

            // Only log if the wireless charging state actually changed or it's a direct power connection event
            if (isWirelessCharging != previouslyWirelessCharging || Intent.ACTION_POWER_CONNECTED.equals(action)) {
                Log.d(TAG, "Wireless charging: " + isWirelessCharging);
            }

            if (isWirelessCharging) {
                if (keyguardManager != null && keyguardManager.isDeviceLocked()) {
                    // Only log if state changed to prevent log spam from BATTERY_CHANGED
                    if (!previouslyWirelessCharging || Intent.ACTION_POWER_CONNECTED.equals(action)) {
                        Log.d(TAG, "Device is locked and wireless charging connected. Waiting for unlock.");
                    }
                    // We'll wait for ACTION_USER_PRESENT
                } else {
                    Log.d(TAG, "Device is already unlocked and wireless charging connected. Triggering action.");
                    triggerFlashlightActivity(context);
                }
            }
        } else if (Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
            isWirelessCharging = false;
            Log.d(TAG, "Power disconnected. Wireless charging: " + isWirelessCharging);
            // Optional: Stop flashlight or any ongoing action if needed
            Intent serviceIntent = new Intent(context, FlashlightService.class);
            serviceIntent.setAction(FlashlightService.ACTION_TURN_OFF_FLASHLIGHT);
            context.startService(serviceIntent);

        } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
            Log.d(TAG, "Device unlocked by user.");
            // Check again if it was due to wireless charging trigger
            // We need to read the sticky battery intent to check current charging status
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, filter); // Okay to pass null for BroadcastReceiver if only reading sticky
            if (batteryStatus != null) {
                int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                isWirelessCharging = (chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS);
            }

            if (isWirelessCharging) {
                Log.d(TAG, "Device unlocked while wireless charging is active. Triggering action.");
                triggerFlashlightActivity(context);
            } else {
                Log.d(TAG, "Device unlocked but not on wireless charging. No action.");
            }
        }
    }

    private void triggerFlashlightActivity(Context context) {
        // We will launch a transparent activity that then starts the service
        // This is because starting a service from a BroadcastReceiver has limitations,
        // especially for foreground services or operations like camera access.
        Intent intent = new Intent(context, TransparentActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    // It's good practice to register this receiver programmatically if it needs to be active
    // only while the app is "running" (even if in background).
    // For a persistent background operation like this, AndroidManifest registration is fine,
    // but be mindful of battery consumption and Android's background restrictions.
    // A foreground service might be necessary for long-running reliable operation.
}
