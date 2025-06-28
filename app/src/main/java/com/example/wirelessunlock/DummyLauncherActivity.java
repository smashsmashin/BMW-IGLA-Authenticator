package com.example.wirelessunlock;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class DummyLauncherActivity extends Activity {
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 101;
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
                // The user will be prompted. The actual service start should ideally wait for this,
                // or handle cases where permission is denied. For simplicity here, we start it regardless.
                // A real app would have a more robust flow.
                Toast.makeText(this, "Requesting notification permission for service.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "WirelessUnlock monitoring service started.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "WirelessUnlock monitoring service started.", Toast.LENGTH_LONG).show();
        }

        // Immediately finish the activity
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted. Service will show status.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied. Service status might not be visible.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
