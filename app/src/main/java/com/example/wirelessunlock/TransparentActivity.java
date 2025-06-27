package com.example.wirelessunlock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class TransparentActivity extends Activity {

    private static final String TAG = "TransparentActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "TransparentActivity created.");

        // Start the FlashlightService to turn on the flashlight
        Intent serviceIntent = new Intent(this, FlashlightService.class);
        serviceIntent.setAction(FlashlightService.ACTION_TURN_ON_FLASHLIGHT);
        startService(serviceIntent);

        // Immediately finish this activity as it's just a trigger
        finish();
    }
}
