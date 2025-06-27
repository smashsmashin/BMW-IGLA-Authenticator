package com.example.wirelessunlock;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class FlashlightService extends Service {

    private static final String TAG = "FlashlightService";
    public static final String ACTION_TURN_ON_FLASHLIGHT = "com.example.wirelessunlock.ACTION_TURN_ON_FLASHLIGHT";
    public static final String ACTION_TURN_OFF_FLASHLIGHT = "com.example.wirelessunlock.ACTION_TURN_OFF_FLASHLIGHT";
    private static final long FLASHLIGHT_OFF_DELAY_MS = 60 * 1000; // 1 minute

    private CameraManager cameraManager;
    private String cameraId;
    private Handler handler;
    private boolean isFlashlightOn = false;

    private final Runnable turnOffRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "Automatically turning off flashlight after delay.");
            setFlashlightState(false);
            stopSelf(); // Stop the service after turning off the flashlight
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "FlashlightService onCreate");
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        handler = new Handler(Looper.getMainLooper());
        try {
            if (cameraManager != null) {
                for (String id : cameraManager.getCameraIdList()) {
                    CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                    Boolean hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                    Integer lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                    if (hasFlash != null && hasFlash && lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                        cameraId = id;
                        break;
                    }
                }
            }
            if (cameraId == null) {
                Log.e(TAG, "No camera with flash found.");
                Toast.makeText(this, "No camera with flash found.", Toast.LENGTH_SHORT).show();
                stopSelf();
            }
        } catch (CameraAccessException | IllegalArgumentException e) { // Added IllegalArgumentException for emulators
            Log.e(TAG, "Error accessing camera: " + e.getMessage());
            Toast.makeText(this, "Error accessing camera.", Toast.LENGTH_SHORT).show();
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "FlashlightService onStartCommand");
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            Log.d(TAG, "Action received: " + action);
            if (ACTION_TURN_ON_FLASHLIGHT.equals(action)) {
                if (cameraId != null) {
                    setFlashlightState(true);
                    // Schedule to turn off
                    handler.removeCallbacks(turnOffRunnable); // Remove any existing callbacks
                    handler.postDelayed(turnOffRunnable, FLASHLIGHT_OFF_DELAY_MS);
                } else {
                    Log.e(TAG, "Camera ID is null, cannot turn on flashlight.");
                    stopSelf(); // Stop if no camera to operate
                }
            } else if (ACTION_TURN_OFF_FLASHLIGHT.equals(action)) {
                if (cameraId != null) {
                    setFlashlightState(false);
                }
                handler.removeCallbacks(turnOffRunnable); // Remove scheduled turn off
                stopSelf(); // Stop the service
            }
        } else {
            Log.d(TAG, "Intent or action is null, stopping service.");
            // If service is started without a specific action, or if intent is null (e.g. service restart)
            // decide appropriate behavior. For now, just stop it.
            if(isFlashlightOn){ // If flashlight was on, ensure it's turned off before stopping.
                setFlashlightState(false);
            }
            stopSelf();
        }
        return START_NOT_STICKY; // If the service is killed, do not recreate it unless an explicit intent is sent.
    }

    private void setFlashlightState(boolean enable) {
        if (cameraManager == null || cameraId == null) {
            Log.e(TAG, "Camera manager or camera ID is null. Cannot set flashlight state.");
            return;
        }
        try {
            cameraManager.setTorchMode(cameraId, enable);
            isFlashlightOn = enable;
            Log.d(TAG, "Flashlight " + (enable ? "ON" : "OFF"));
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error setting torch mode: " + e.getMessage());
            Toast.makeText(this, "Error controlling flashlight.", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException e) { // Catching exception often seen in emulators if camera is busy or unavailable
             Log.e(TAG, "Error setting torch mode (IllegalArgumentException): " + e.getMessage());
             Toast.makeText(this, "Could not access camera for flashlight.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "FlashlightService onDestroy");
        // Ensure flashlight is turned off when service is destroyed
        if (isFlashlightOn) {
            setFlashlightState(false);
        }
        handler.removeCallbacks(turnOffRunnable); // Clean up handler callbacks
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not a bound service
    }
}
