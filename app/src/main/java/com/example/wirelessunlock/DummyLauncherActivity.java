package com.example.wirelessunlock;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri; // Added import
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings; // Added import
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class DummyLauncherActivity extends Activity {
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 101;
    private static final int SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST_CODE = 102;
    private static final String TAG = "DummyLauncherActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start the PowerMonitoringService
        Intent serviceIntent = new Intent(this, PowerMonitoringService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        // Check and request POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 (API 33)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }

        // Check and request SYSTEM_ALERT_WINDOW permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Available from API 23
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                // Before starting activity, show a toast or dialog explaining why this permission is needed.
                Toast.makeText(this, "Please grant 'Draw over other apps' permission for full functionality.", Toast.LENGTH_LONG).show();
                startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST_CODE);
                // Note: We finish() immediately after. The user will grant permission and return to whatever screen they were on.
                // The service will attempt to start activities later; hopefully, by then, the permission is granted.
                // A more robust implementation might delay certain actions until this permission is confirmed via onActivityResult.
            }
        }

        Toast.makeText(this, "WirelessUnlock monitoring service started/checked.", Toast.LENGTH_LONG).show();
        // Immediately finish the activity
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "Draw over other apps permission granted.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Draw over other apps permission not granted.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
