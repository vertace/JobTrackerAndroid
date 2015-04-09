package com.tt.Notifications;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.tt.fragments.TaskListFragment;
import com.tt.jobtracker.MainActivity;
import com.tt.jobtracker.R;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;


    public GcmIntentService() {
        super("GcmIntentService");
    }

    public static final String TAG = "GCM Demo";

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) { // has effect of unparcelling Bundle
            /*
			 * Filter messages based on message type. Since it is likely that
			 * GCM will be extended in the future with new message types, just
			 * ignore any message types you're not interested in, or that you
			 * don't recognize.
			 */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
                    .equals(messageType)) {
                // sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
                    .equals(messageType)) {
                // sendNotification("Deleted messages on server: " +
                // extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
                    .equals(messageType)) {
                String title = extras.getString("title");
                String message = extras.getString("message");

                GetNewlyAddedTaskFromServer();

                SendNotification(title, message);

            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void GetNewlyAddedTaskFromServer() {
        new TaskListFragment().DownloadTasksFromServer();
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    protected void SendNotification(String title, String msg) {

        // Invoking the default notification service
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this);

        mBuilder.setContentTitle(title);
        mBuilder.setContentText(msg);
        mBuilder.setTicker(msg);
        mBuilder.setSmallIcon(R.drawable.ic_launcher);
        mBuilder.setSound(RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        mBuilder.setAutoCancel(true);

        mBuilder.getNotification().flags |= Notification.FLAG_AUTO_CANCEL;

        Intent resultIntent = new Intent(this, MainActivity.class);
        int notificationIdOne = 111;
        resultIntent.putExtra("notificationId", notificationIdOne);

        // This ensures that navigating backward from the Activity leads out of
        // the app to Home page
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent
        stackBuilder.addParentStack(MainActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_ONE_SHOT // can only be used once
        );
        // start the activity when the user clicks the notification text
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager myNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // pass the Notification object to the system
        myNotificationManager.notify(notificationIdOne, mBuilder.build());
    }
}
