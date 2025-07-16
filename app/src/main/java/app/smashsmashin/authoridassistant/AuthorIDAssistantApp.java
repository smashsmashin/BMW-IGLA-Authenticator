package app.smashsmashin.authoridassistant;

import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;

public class AuthorIDAssistantApp extends Application {

    private static final String LOG_TAG = "AuthorIDAssistant";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "Application onCreate - Process created.");
        // This is a good place for one-time initializations if needed.
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "Application onTerminate - Process terminating.");
        // This method is not guaranteed to be called, especially on production devices.
        // It's mainly for emulators or specific scenarios.
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "Application onConfigurationChanged");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.w(TAG, "Application onLowMemory");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.w(TAG, "Application onTrimMemory, level: " + level);
        // TRIM_MEMORY_COMPLETE means the process is about to be killed.
        // Other levels indicate varying degrees of memory pressure.
        if (level == TRIM_MEMORY_COMPLETE) {
            Log.w(TAG, "Application onTrimMemory: TRIM_MEMORY_COMPLETE - Process likely to be killed soon.");
        }
    }
}
