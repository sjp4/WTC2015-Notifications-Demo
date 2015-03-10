package com.getpebble.techcon;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;
import android.os.Bundle;
import android.support.v4.app.RemoteInput;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class NotificationActivity extends Activity {
    private static final String ACTION_REPLY = "com.getpebble.techcon.REPLY";
    private static final int NOTIFICATION_ID_MAIN = 1;
    private static final String TAG = "tag1";
    private static final String EXTRA_MESSAGE_ID = "extra_message_id";
    private static final String KEY_REPLY_TEXT = "reply_text";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        Button firstButton = (Button) findViewById(R.id.first_button);
        Button secondButton = (Button) findViewById(R.id.second_button);
        final TextView response = (TextView) findViewById(R.id.response);

        firstButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFirstNotification();
            }
        });

        secondButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSecondNotification();
            }
        });

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Receive the result of the Wearable action (this could be in a background service)
                int messageId = intent.getIntExtra(EXTRA_MESSAGE_ID, -1);
                Bundle results = RemoteInput.getResultsFromIntent(intent);
                CharSequence reply = results.getCharSequence(KEY_REPLY_TEXT);
                response.setText("Received reply action for message " + messageId + " reply = '" + reply + "'");
            }
        };
        registerReceiver(receiver, new IntentFilter(ACTION_REPLY));
    }

    private static final String TITLE_MESSAGE_FROM_HEIKO = "Message from Heiko";
    private static final String CONTENT_MESSAGE_FROM_HEIKO = "When does Wearables TechCon start";
    private static final String TICKER_MESSAGE_FROM_HEIKO = "Heiko: When does Wearables TechCon start?";

    private void sendFirstNotification() {
        Notification notification = getNotification(TITLE_MESSAGE_FROM_HEIKO, CONTENT_MESSAGE_FROM_HEIKO, TICKER_MESSAGE_FROM_HEIKO, 111, true);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(TAG, NOTIFICATION_ID_MAIN, notification);
    }

    private static final String TITLE_MESSAGE_FROM_STEVE = "2 new messages";
    private static final String CONTENT_MESSAGE_FROM_STEVE  = "2 new messages from Heiko and Steve";
    private static final String TICKER_MESSAGE_FROM_STEVE = "Steve: It already started!";

    private void sendSecondNotification() {
        Notification notification = getNotification(TITLE_MESSAGE_FROM_STEVE, CONTENT_MESSAGE_FROM_STEVE, TICKER_MESSAGE_FROM_STEVE, 222, false);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(TAG, NOTIFICATION_ID_MAIN, notification);
    }

    String[] REPLIES = { "I'll get back to you", "Yes", "No", "Phrasing" };

    private Notification getNotification(String title, String content, String tickerText, int messageId, boolean addActions) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.drawable.ic_menu_end_conversation);
        builder.setTicker(tickerText);
        builder.setContentTitle(title);
        builder.setContentText(content);

        if (addActions) {
            // Action for phone
            Intent intentReplyAndroid = new Intent(this, NotificationActivity.class);
            PendingIntent pendingIntentReplyAndroid = PendingIntent.getActivity(this, 0, intentReplyAndroid, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(R.drawable.ic_menu_revert, "Reply", pendingIntentReplyAndroid);

            // Action for Wearable
            Intent intentReplyWearable = new Intent(ACTION_REPLY);
            intentReplyWearable.putExtra(EXTRA_MESSAGE_ID, messageId);
            PendingIntent pendingIntentReplyWearable = PendingIntent.getBroadcast(this, 0, intentReplyWearable, 0);
            RemoteInput remoteInput = new RemoteInput.Builder(KEY_REPLY_TEXT)
                    .setLabel("Reply")
                    .setChoices(REPLIES)
                    .build();
            NotificationCompat.Action wearableReplyAction =
                    new NotificationCompat.Action.Builder(R.drawable.ic_menu_revert, "Reply", pendingIntentReplyWearable)
                            .extend(new NotificationCompat.Action.WearableExtender().setAvailableOffline(false))
                            .addRemoteInput(remoteInput)
                            .build();
            builder.extend(new NotificationCompat.WearableExtender().addAction(wearableReplyAction));
        }

        return builder.build();
    }
}
