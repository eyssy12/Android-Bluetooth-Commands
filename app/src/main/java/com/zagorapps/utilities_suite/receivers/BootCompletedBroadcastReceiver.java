package com.zagorapps.utilities_suite.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zagorapps.utilities_suite.services.persistence.ClipboardService;

/**
 * Created by eyssy on 21/01/2017.
 */

public class BootCompletedBroadcastReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent startServiceIntent = new Intent(context, ClipboardService.class);
        context.startService(startServiceIntent);
    }
}