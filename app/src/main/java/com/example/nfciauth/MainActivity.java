package com.example.nfciauth;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivityNFC";

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    private TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Layout will be created in a later step

        statusTextView = findViewById(R.id.status_text_view); // ID from activity_main.xml
        statusTextView.setText("Waiting for NFC tag...");

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not available on this device.", Toast.LENGTH_LONG).show();
            statusTextView.setText("NFC not available.");
            // Optionally finish the activity or disable NFC specific features
            // finish();
            // return;
        } else if (!nfcAdapter.isEnabled()) {
            statusTextView.setText("NFC is disabled. Please enable it in settings.");
            Toast.makeText(this, "Please enable NFC in Settings.", Toast.LENGTH_LONG).show();
            // Optionally, direct user to NFC settings
            // startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
        }


        // Create a PendingIntent object so the Android system can populate it with the details of the tag when it is scanned.
        // FLAG_MUTABLE is required for apps targeting Android 12 (S) or higher.
        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE
        );

        // Setup an intent filter for all NDEF tags
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("text/plain"); // Specific NDEF type, adjust if needed
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Failed to add MIME type.", e);
        }

        // Setup an intent filter for all TECH tags (fallback)
        IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);

        intentFiltersArray = new IntentFilter[]{ndef, tech};

        // Setup a tech list for all NfcF tags (or other technologies as needed)
        techListsArray = new String[][]{
                new String[]{Ndef.class.getName()} // Example: only interested in NDEF formatted tags
                // You can add more tech types here, e.g., new String[]{IsoDep.class.getName()}
        };

        // Biometric setup
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(MainActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(), "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                statusTextView.setText("Biometric authentication error: " + errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(), "Authentication succeeded!", Toast.LENGTH_SHORT).show();
                statusTextView.setText("Biometric authentication succeeded. Enabling Author ID...");
                // Launch AuthorIdActivity
                Intent intent = new Intent(MainActivity.this, AuthorIdActivity.class);
                startActivity(intent);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
                statusTextView.setText("Biometric authentication failed.");
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for NFC Auth")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Use account password") // Or "Cancel"
                 //.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .setConfirmationRequired(false) // Set to true if you want user to confirm after successful auth
                .build();

        // Check if biometric hardware is available and enrolled
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d(TAG, "App can authenticate using biometrics.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.e(TAG, "No biometric features available on this device.");
                Toast.makeText(this, "No biometric features available on this device.", Toast.LENGTH_LONG).show();
                statusTextView.setText("Biometric hardware not available.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.e(TAG, "Biometric features are currently unavailable.");
                Toast.makeText(this, "Biometric features are currently unavailable.", Toast.LENGTH_LONG).show();
                statusTextView.setText("Biometric hardware unavailable.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Log.e(TAG, "The user hasn't associated any biometric credentials with their account.");
                // Prompts the user to create credentials that your app accepts.
                final Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BiometricManager.Authenticators.BIOMETRIC_STRONG);
                // Consider launching this intent with startActivityForResult if you need to react to enrollment.
                // startActivity(enrollIntent);
                Toast.makeText(this, "No biometric credentials enrolled. Please enroll in settings.", Toast.LENGTH_LONG).show();
                statusTextView.setText("No biometric credentials enrolled.");
                break;
            default:
                Log.e(TAG, "Biometric status unknown.");
                Toast.makeText(this, "Biometric status unknown.", Toast.LENGTH_LONG).show();
                statusTextView.setText("Biometric status unknown.");
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null && nfcAdapter.isEnabled()) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
            statusTextView.setText("Waiting for NFC tag...");
        } else if (nfcAdapter != null && !nfcAdapter.isEnabled()) {
            statusTextView.setText("NFC is disabled. Please enable it in settings.");
             Toast.makeText(this, "Please enable NFC in Settings.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction();
        statusTextView.setText("NFC Tag Detected!");
        Log.d(TAG, "NFC Intent Action: " + action);

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                Log.d(TAG, "NFC Tag detected: " + tag.toString());
                // Here, you could read data from the tag if needed.
                // For this app, just detecting the tag is enough to trigger biometrics.

                // Check if biometrics can be used
                BiometricManager biometricManager = BiometricManager.from(this);
                if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
                    statusTextView.setText("NFC Tag Scanned. Authenticate to continue.");
                    biometricPrompt.authenticate(promptInfo);
                } else {
                     Toast.makeText(this, "Biometric authentication not available or not enrolled.", Toast.LENGTH_LONG).show();
                     statusTextView.setText("Biometric authentication not set up.");
                }
            } else {
                Log.e(TAG, "NFC Tag is null.");
                statusTextView.setText("Error reading NFC Tag.");
            }
        }
    }
}
