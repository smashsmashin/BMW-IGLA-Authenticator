package com.example.wirelessunlock;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.os.Binder;
import android.os.IBinder;
import android.content.Intent;

public class MyNotificationListenerService extends NotificationListenerService {

    private static final String TAG = "MyNotificationListener";
    private static final String TARGET_APP_PACKAGE = "com.dma.author.authorid";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        // We will no longer use this method.
    }

    public void uncheckToggleButton() {
        for (StatusBarNotification sbn : getActiveNotifications()) {
            if (TARGET_APP_PACKAGE.equals(sbn.getPackageName())) {
                Notification notification = sbn.getNotification();
                if (notification != null && notification.contentIntent != null) {
                    String title = notification.extras.getString(Notification.EXTRA_TITLE);
                    String text = notification.extras.getString(Notification.EXTRA_TEXT);
                    if ("Author ID".equals(title) && "Key FOB activated".equals(text)) {
                        try {
                            ClickState.shouldClick = true;
                            ClickState.shouldUnclick = true;
                            notification.contentIntent.send();
                        } catch (Exception e) {
                            Log.e(TAG, "Error sending pending intent", e);
                        }
                        break; // We found the notification, no need to continue.
                    }
                }
            }
        }
    }

    public class LocalBinder extends Binder {
        MyNotificationListenerService getService() {
            return MyNotificationListenerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }
}
