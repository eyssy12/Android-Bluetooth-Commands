package com.zagorapps.utilities_suite.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by eyssy on 29/12/2016.
 */
public class CustomViewPager extends android.support.v4.view.ViewPager
{
    private boolean swipeEnabled;

    public CustomViewPager(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        this.swipeEnabled = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        return this.swipeEnabled && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event)
    {
        return this.swipeEnabled && super.onInterceptTouchEvent(event);
    }

    public void setSwipeEnabled(boolean swipeEnabled)
    {
        this.swipeEnabled = swipeEnabled;
    }

    public boolean isSwipeEnabled()
    {
        return this.swipeEnabled;
    }
}
