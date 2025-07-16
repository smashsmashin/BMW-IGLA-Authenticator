package app.smashsmashin.authoridassistant;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import java.util.List;

public class AuthorIDAccessibilityService extends AccessibilityService {
    private static final String LOG_TAG = "AuthorIDAssistant";
    private static final String TARGET_APP_PACKAGE = "com.dma.author.authorid";
    private static final String TAG_ACTIVITY_NAME = "com.dma.author.authorid.view.TagActivity";
    private static final String TOGGLE_BUTTON_ID = "com.dma.author.authorid:id/acb_tag_on_off";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!AppState.isKeyFobActionPending) {
            return;
        }

        int eventType = event.getEventType();
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {

            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode == null) {
                Log.d(LOG_TAG, "Root node is null.");
                return;
            }

            String currentActivityName = event.getClassName().toString();
            Log.d(LOG_TAG, "Current activity: " + currentActivityName);

            if (TAG_ACTIVITY_NAME.equals(currentActivityName)) {
                Log.d(LOG_TAG, "TagActivity reached. Attempting to find and click the toggle button.");
                findAndClickToggleButton(rootNode);
                AppState.isKeyFobActionPending = false; // Reset the state
            } else {
                Log.d(LOG_TAG, "Not in TagActivity. Current activity: " + currentActivityName + ". Waiting...");
            }
            rootNode.recycle();
        }
    }

    private void findAndClickToggleButton(AccessibilityNodeInfo rootNode) {
        List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByViewId(TOGGLE_BUTTON_ID);
        if (nodes != null && !nodes.isEmpty()) {
            AccessibilityNodeInfo toggleButton = nodes.get(0);
            Log.d(LOG_TAG, "ToggleButton found. Is checked: " + toggleButton.isChecked());

            if (AppState.shouldActivate && !toggleButton.isChecked()) {
                Log.d(LOG_TAG, "ToggleButton is not checked. Performing click to activate.");
                toggleButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            } else if (!AppState.shouldActivate && toggleButton.isChecked()) {
                Log.d(LOG_TAG, "ToggleButton is checked. Performing click to deactivate.");
                toggleButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            } else {
                Log.d(LOG_TAG, "ToggleButton is already in the desired state. No action needed.");
            }
        } else {
            Log.w(LOG_TAG, "ToggleButton with ID " + TOGGLE_BUTTON_ID + " not found.");
        }
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED |
                          AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        info.packageNames = new String[]{TARGET_APP_PACKAGE};
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        setServiceInfo(info);
        Log.d(LOG_TAG, "Accessibility service connected and configured.");
    }

    @Override
    public void onInterrupt() {
        Log.d(LOG_TAG, "Accessibility Service interrupted.");
    }
}
