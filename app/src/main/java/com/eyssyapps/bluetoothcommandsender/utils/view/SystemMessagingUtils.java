package com.eyssyapps.bluetoothcommandsender.utils.view;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import com.eyssyapps.bluetoothcommandsender.services.NotificationPublisherService;

import java.util.Date;
import java.util.Random;

/**
 * Created by eyssy on 29/02/2016.
 */
public class SystemMessagingUtils
{
    public static void scheduleNotification(
            @NonNull Context context,
            @NonNull Class<?> intentClass,
            @NonNull Notification notification,
            Date notificationKickoff)
    {
        SystemMessagingUtils.scheduleNotification(context, intentClass, notification, notificationKickoff.getTime());
    }

    public static void scheduleNotification(
            @NonNull Context context,
            @NonNull Class<?> intentClass,
            @NonNull Notification notification,
            long notificationKickoffInMs)
    {
        int notificationId = notification.extras.getInt(NotificationPublisherService.NOTIFICATION_ID, NotificationPublisherService.NOTIFICATION_ID_DEFAULT_VALUE);

        if (notificationId == NotificationPublisherService.NOTIFICATION_ID_DEFAULT_VALUE)
        {
            // log that the notification didnt have an ID setup.
            notificationId = new Random().nextInt();
        }

        Intent notificationIntent = new Intent(context, intentClass);
        notificationIntent.putExtra(NotificationPublisherService.NOTIFICATION_ID, notificationId);
        notificationIntent.putExtra(NotificationPublisherService.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, notificationKickoffInMs, pendingIntent);
    }

    public static Snackbar createSnackbar(@NonNull View parentView, String text, int length)
    {
        return Snackbar.make(parentView, text, length);
    }

    public static Snackbar createSnackbar(@NonNull View parentView, String text, int length, String actionName, View.OnClickListener listener)
    {
        return SystemMessagingUtils
                .createSnackbar(parentView, text, length)
                .setAction(actionName, listener);
    }

    public static Toast createToast(Context context, String text, int length)
    {
        return Toast.makeText(context, text, length);
    }

    public static void showSnackBar(View parentView, String text, int length)
    {
        SystemMessagingUtils.createSnackbar(parentView, text, length).show();
    }

    public static void showToast(Context context, String text, int length)
    {
        SystemMessagingUtils.createToast(context, text, length).show();
    }

    public static void showShortToast(Context context, String text)
    {
        SystemMessagingUtils.createToast(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void showLongToast(Context context, String text)
    {
        SystemMessagingUtils.createToast(context, text, Toast.LENGTH_LONG).show();
    }

    public static Notification createNotification(@NonNull Context context, String title, String content, int iconResourceId, int notificationId)
    {
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setSmallIcon(iconResourceId);

        Bundle bundle = new Bundle();
        bundle.putInt(NotificationPublisherService.NOTIFICATION_ID, notificationId);
        builder.addExtras(bundle);

        return builder.build();
    }
}