package com.zagorapps.utilities_suite.utils.view;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;

/**
 * Created by eyssy on 01/09/2016.
 */
public class ActivityUtils
{
    public static void finish(@NonNull Activity activity, int resultCode)
    {
        ActivityUtils.finish(activity, resultCode, null);
    }

    public static void finish(@NonNull Activity activity, int resultCode, Intent intent)
    {
        if (intent == null)
        {
            intent = new Intent();
        }

        activity.setResult(resultCode, intent);
        activity.finish();
    }
}