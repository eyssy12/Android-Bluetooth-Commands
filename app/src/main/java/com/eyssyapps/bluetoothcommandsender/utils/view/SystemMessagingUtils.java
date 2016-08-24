package com.eyssyapps.bluetoothcommandsender.utils.view;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

/**
 * Created by eyssy on 29/02/2016.
 */
public class SystemMessagingUtils
{
    public static Snackbar createSnackbar(View parentView, String text, int length)
    {
        return Snackbar.make(parentView, text, length);
    }

    public static Snackbar createSnackbar(View parentView, String text, int length, String actionName, View.OnClickListener listener)
    {
        return SystemMessagingUtils.createSnackbar(parentView, text, length).setAction(actionName,
                listener);
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
}