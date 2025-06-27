package com.example.wirelessunlock;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class DummyLauncherActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Optional: Show a toast or simple message
        Toast.makeText(this, "WirelessUnlock background service is active.", Toast.LENGTH_LONG).show();
        // Immediately finish the activity
        finish();
    }
}
