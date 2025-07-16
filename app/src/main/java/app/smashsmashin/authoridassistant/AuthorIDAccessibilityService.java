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

    private void findAndClickToggleButton(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }

        if ("android.widget.ToggleButton".equals(nodeInfo.getClassName())) {
            if (nodeInfo.isClickable()) {
                if (AppState.shouldActivate && !nodeInfo.isChecked()) {
                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    Log.d(LOG_TAG, "Clicked ToggleButton");
                } else if (!AppState.shouldActivate && nodeInfo.isChecked()) {
                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    Log.d(LOG_TAG, "Unclicked ToggleButton");
                } else {
                    Log.d(LOG_TAG, "ToggleButton is already in the desired state.");
                }
                performGlobalAction(GLOBAL_ACTION_BACK);
                return;
            }
        }

        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            findAndClickToggleButton(nodeInfo.getChild(i));
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
