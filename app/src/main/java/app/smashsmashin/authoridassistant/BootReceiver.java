package app.smashsmashin.authoridassistant;

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
public class BootReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "AuthorIDAssistant";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(LOG_TAG, "Received action: " + action);

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            Log.d(LOG_TAG, "BOOT_COMPLETED received. Starting MainService.");
            Intent serviceIntent = new Intent(context, MainService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }
    }
}
