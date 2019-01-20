package com.example.footcare.diabeticcarethermal;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
//Taken from https://www.youtube.com/watch?v=1fV9NmvxXJo&t=461s
public class NotifyService extends BroadcastReceiver {
    DatabaseHelper myDB;
    @Override
    public void onReceive(Context context, Intent intent) {

        String title = intent.getStringExtra("Title");
        String text = intent.getStringExtra("Text");
        createNotification(context, title, text, "Alert");
//        myDB = new DatabaseHelper(context);
//        Cursor res = myDB.getAllData(DatabaseHelper.TABLE_NOTIFICATIONS);
//        if (res.getCount() == 0){
//            createNotification(context, "Diabetic Foot Care", "Take Your Daily Foot Photo", "Alert");
//            //createJournalNotification(context, "Journal Entry", "Complete Your Journal Entry", "Alert");
//          }
//        while (res.moveToNext()){
//            if (res.getInt(2) != 0){
//                createNotification(context, "Diabetic Foot Care", "Take Your Daily Foot Photo", "Alert");
//                //createJournalNotification(context, "Journal Entry", "Complete Your Journal Entry", "Alert");
//            }
//        }

    }

    public void createNotification(Context context, String msg, String msgText, String msgAlert) {
        PendingIntent notificIntent = PendingIntent.getActivity(context, 0, new Intent(context, TakeFootImage.class), 0);

        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.foot)
                .setContentTitle(msg)
                .setTicker(msgAlert)
                .setContentText(msgText);

        mBuilder.setContentIntent(notificIntent);
        mBuilder.setAutoCancel(true);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }
//    public void createJournalNotification(Context context, String msg, String msgText, String msgAlert) {
//        PendingIntent notificationIntent = PendingIntent.getActivity(context, 1, new Intent(context, Journal.class), 0);
//
//        NotificationCompat.Builder jBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(context)
//                .setSmallIcon(R.drawable.journal)
//                .setContentTitle(msg)
//                .setTicker(msgAlert)
//                .setContentText(msgText);
//
//        jBuilder.setContentIntent(notificationIntent);
//        jBuilder.setAutoCancel(true);
//
//        NotificationManager jNotificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
//        jNotificationManager.notify(2, jBuilder.build());
//    }
}
