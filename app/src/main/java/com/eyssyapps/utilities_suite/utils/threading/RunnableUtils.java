package com.eyssyapps.utilities_suite.utils.threading;

/**
 * Created by eyssy on 04/12/2015.
 */
public class RunnableUtils
{
    public static void ExecuteWithDelay(Runnable runnable, long delayInMillis)
    {
        new android.os.Handler().postDelayed(runnable, delayInMillis);
    }
}
