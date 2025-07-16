package com.example.wirelessunlock;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.os.Binder;
import android.os.IBinder;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class MyNotificationListenerService extends NotificationListenerService {

    private static final String TAG = "MyNotificationListener";
    private static final String TARGET_APP_PACKAGE = "com.dma.author.authorid";
    public static final String ACTION_UNCHECK_TOGGLE = "com.example.wirelessunlock.UNCHECK_TOGGLE";
    private static final String CHANNEL_ID = "NotificationListenerChannel";
    private static final int NOTIFICATION_ID = 2;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_UNCHECK_TOGGLE.equals(intent.getAction())) {
                uncheckToggleButton();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, new Notification());
        IntentFilter filter = new IntentFilter(ACTION_UNCHECK_TOGGLE);
        registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    public void uncheckToggleButton() {
        Log.d(TAG, "uncheckToggleButton called.");
        for (StatusBarNotification sbn : getActiveNotifications()) {
            Log.d(TAG, "Found notification: " + sbn.getPackageName());
            if (TARGET_APP_PACKAGE.equals(sbn.getPackageName())) {
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
                        break; // We found the notification, no need to continue.
                    }
                }
            }
        }
        Log.d(TAG, "Finished uncheckToggleButton.");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Notification Listener Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
