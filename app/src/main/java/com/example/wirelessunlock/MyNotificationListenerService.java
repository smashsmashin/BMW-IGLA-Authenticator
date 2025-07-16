package com.example.wirelessunlock;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class MyNotificationListenerService extends NotificationListenerService {

    private static final String TAG = "MyNotificationListener";
    private static final String TARGET_APP_PACKAGE = "com.dma.author.authorid";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d(TAG, "onNotificationPosted: " + sbn.getPackageName());
        if (TARGET_APP_PACKAGE.equals(sbn.getPackageName())) {
            Log.d(TAG, "Found notification from target package.");
            Notification notification = sbn.getNotification();
            if (notification != null && notification.contentIntent != null) {
                String title = notification.extras.getString(Notification.EXTRA_TITLE);
                String text = notification.extras.getString(Notification.EXTRA_TEXT);
                Log.d(TAG, "Notification title: " + title);
                Log.d(TAG, "Notification text: " + text);
                if ("Author ID".equals(title) && "Key FOB activated".equals(text)) {
                    Log.d(TAG, "Found target notification. Sending intent.");
                    try {
                        ClickState.shouldClick = true;
                        ClickState.shouldUnclick = true;
                        notification.contentIntent.send();
                    } catch (Exception e) {
                        Log.e(TAG, "Error sending pending intent", e);
                    }
                }
            }
        }
    }
}
