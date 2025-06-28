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

// This receiver is now primarily for BOOT_COMPLETED (to start the service)
// and other diagnostic intents if we keep them in the manifest.
// Power-related intents will be handled by the receiver within PowerMonitoringService.
public class PowerConnectionReceiver extends BroadcastReceiver {

    private static final String TAG = "ManifestReceiver"; // Changed TAG for clarity

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Received action: " + action);

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            Log.d(TAG, "BOOT_COMPLETED received. Starting PowerMonitoringService.");
            Intent serviceIntent = new Intent(context, PowerMonitoringService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        } else if (Intent.ACTION_TIMEZONE_CHANGED.equals(action) ||
                   "android.intent.action.HEADSET_PLUG".equals(action) || // Using string to avoid SDK check if not available
                   Intent.ACTION_SCREEN_ON.equals(action) ||
                   Intent.ACTION_SCREEN_OFF.equals(action)) {
            // Log diagnostic events if they are still registered in manifest
            Log.d(TAG, "Diagnostic event received: " + action);
            if ("android.intent.action.HEADSET_PLUG".equals(action)) {
                int state = intent.getIntExtra("state", -1);
                Log.d(TAG, "HEADSET_PLUG state: " + state);
            }
        }
        // Power-related actions (ACTION_POWER_CONNECTED, DISCONNECTED, BATTERY_CHANGED, USER_PRESENT for power context)
        // are now primarily handled by the programmatically registered receiver in PowerMonitoringService.
        // We can remove them from this receiver's manifest filter eventually if the service handles them reliably.
    }

    // triggerFlashlightActivity is removed as this receiver will not directly trigger it anymore.
    // That logic is now effectively within PowerMonitoringService's own receiver.
}
