package com.example.wirelessunlock;

import android.accessibilityservice.AccessibilityService;
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

public class MyAccessibilityService extends AccessibilityService {

    private static final String TAG = "MyAccessibilityService";
    private static final String TARGET_APP_PACKAGE = "com.dma.author.authorid";
    private static final String TARGET_ACTIVITY_NAME = "com.dma.author.authorid.view.TagActivity";

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Log.d(TAG, "onAccessibilityEvent: " + event.toString());
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String packageName = event.getPackageName().toString();
            String className = event.getClassName().toString();

            // Log.d(TAG, "Window state changed: " + packageName + "/" + className);

            if (TARGET_APP_PACKAGE.equals(packageName) && TARGET_ACTIVITY_NAME.equals(className)) {
                if (ClickState.shouldClick) {
                    if (ClickState.shouldUnclick) {
                        Log.d(TAG, "TagActivity opened by our app, attempting to unclick button.");
                        clickButtonInCenter(false);
                        ClickState.shouldUnclick = false;
                    } else {
                        Log.d(TAG, "TagActivity opened by our app, attempting to click button.");
                        clickButtonInCenter(true);
                    }
                    ClickState.shouldClick = false;
                } else {
                    Log.d(TAG, "TagActivity opened, but not by our app. Ignoring.");
                }
            }
        }
    }

    private void clickButtonInCenter(boolean click) {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "Root node is null, cannot perform click.");
            return;
        }

        if (click) {
            findAndClickButton(rootNode);
        } else {
            findAndUnclickButton(rootNode);
        }
        rootNode.recycle();
    }

    private void findAndClickButton(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }

        if ("android.widget.ToggleButton".equals(nodeInfo.getClassName())) {
            if (nodeInfo.isClickable()) {
                if (!nodeInfo.isChecked()) {
                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    Log.d(TAG, "Clicked ToggleButton");
                } else {
                    Log.d(TAG, "ToggleButton is already active, not clicking.");
                }
                // Start a 1-minute timer
                timerHandler.postDelayed(timerRunnable, 45000);
                Log.d(TAG, "Started 1-minute timer.");
                performGlobalAction(GLOBAL_ACTION_BACK);
                return;
            }
        }

        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            findAndClickButton(nodeInfo.getChild(i));
        }
    }

    private void findAndUnclickButton(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }

        if ("android.widget.ToggleButton".equals(nodeInfo.getClassName())) {
            if (nodeInfo.isClickable()) {
                if (nodeInfo.isChecked()) {
                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    Log.d(TAG, "Unclicked ToggleButton");
                    performGlobalAction(GLOBAL_ACTION_BACK);
                } else {
                    Log.d(TAG, "ToggleButton is already inactive, not unclicking.");
                }
                return;
            }
        }

        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            findAndUnclickButton(nodeInfo.getChild(i));
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

        timerRunnable = () -> {
            Log.d(TAG, "1-minute timer finished. Checking button state.");
            // Restart the service
            if (isServiceRunning(MyAccessibilityService.class)) {
                Log.d(TAG, "Service is running, attempting to unclick button.");

                Intent intent = new Intent();
                intent.setClassName(TARGET_APP_PACKAGE, "com.dma.author.authorid.view.SplashActivity");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                // Set a flag to indicate that the next click should be an unclick
                ClickState.shouldClick = true;
                ClickState.shouldUnclick = true;
            }
        };
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
