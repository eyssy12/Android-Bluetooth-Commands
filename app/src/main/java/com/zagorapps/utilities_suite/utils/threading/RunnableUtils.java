package com.zagorapps.utilities_suite.utils.threading;

/**
 * Created by eyssy on 04/12/2015.
 */
public class RunnableUtils
{
    public static void executeWithDelay(Runnable runnable, long delayInMillis)
    {
        new android.os.Handler().postDelayed(runnable, delayInMillis);
    }
    
    public static void cancelRunnable(Runnable runnable)
    {
        new android.os.Handler().removeCallbacks(runnable);
    }
}