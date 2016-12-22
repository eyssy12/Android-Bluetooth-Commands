package com.eyssyapps.utilities_suite.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

/**
 * Created by eyssy on 01/09/2016.
 */
public class NotificationPublisherService extends BroadcastReceiver
{
    public static String NOTIFICATION = "notification",
            NOTIFICATION_ID = "notification_id";

    public static int NOTIFICATION_ID_DEFAULT_VALUE = -100;

    private static NotificationPublisherService SERVICE;

    private Context context;

    public NotificationPublisherService getDefaultService(@NonNull Context context)
    {
        if (SERVICE == null)
        {
            SERVICE = new NotificationPublisherService(context);
        }

        return SERVICE;
    }

    private NotificationPublisherService(Context context)
    {
        this.context = context;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        // TODO: implement
    }
}
