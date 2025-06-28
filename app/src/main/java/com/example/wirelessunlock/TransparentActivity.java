package com.example.wirelessunlock;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class TransparentActivity extends Activity {

    private static final String TAG = "TransparentActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "TransparentActivity created. Attempting to open settings.");

        // Intent to open device settings
        Intent settingsIntent = new Intent(Settings.ACTION_SETTINGS);
        settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Important when starting from non-Activity context sometimes, good here too.

        try {
            startActivity(settingsIntent);
            Log.d(TAG, "ACTION_SETTINGS intent sent.");
            Toast.makeText(this, "Opening Settings as a test...", Toast.LENGTH_LONG).show();
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Could not open settings: " + e.getMessage());
            Toast.makeText(this, "Could not open settings.", Toast.LENGTH_LONG).show();
        }

        // Immediately finish this activity as it's just a trigger
        finish();
    }
}
