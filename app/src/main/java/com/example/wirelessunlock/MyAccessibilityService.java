package com.example.wirelessunlock;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class MyAccessibilityService extends AccessibilityService {

    private static final String TAG = "MyAccessibilityService";
    private static final String TARGET_APP_PACKAGE = "com.dma.author.authorid";
    private static final String TARGET_ACTIVITY_NAME = "com.dma.author.authorid.view.TagActivity";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Log.d(TAG, "onAccessibilityEvent: " + event.toString());
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String packageName = event.getPackageName().toString();
            String className = event.getClassName().toString();

            // Log.d(TAG, "Window state changed: " + packageName + "/" + className);

            if (TARGET_APP_PACKAGE.equals(packageName) && TARGET_ACTIVITY_NAME.equals(className)) {
                if (ClickState.shouldClick) {
                    Log.d(TAG, "TagActivity opened by our app, attempting to click button.");
                    clickButtonInCenter();
                    ClickState.shouldClick = false;
                } else {
                    Log.d(TAG, "TagActivity opened, but not by our app. Ignoring.");
                }
            }
        }
    }

    private void clickButtonInCenter() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "Root node is null, cannot perform click.");
            return;
        }

        findAndClickButton(rootNode);
        rootNode.recycle();
    }

    private void findAndClickButton(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }

        if (nodeInfo.isClickable()) {
            Rect bounds = new Rect();
            nodeInfo.getBoundsInScreen(bounds);
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int screenHeight = getResources().getDisplayMetrics().heightPixels;

            if (bounds.contains(screenWidth / 2, screenHeight / 2)) {
                Log.d(TAG, "Button isSelected: " + nodeInfo.isSelected());
                Log.d(TAG, "Button isChecked: " + nodeInfo.isChecked());
                Log.d(TAG, "Button getText: " + nodeInfo.getText());
                Log.d(TAG, "Button getContentDescription: " + nodeInfo.getContentDescription());
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Log.d(TAG, "Clicked a button at: " + bounds);
                return;
            }
        }

        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            findAndClickButton(nodeInfo.getChild(i));
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted.");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Accessibility service connected.");
    }
}
