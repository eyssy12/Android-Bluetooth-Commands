package com.zagorapps.utilities_suite;

import android.app.Application;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * Created by eyssy on 05/02/2017.
 */

public class UtilitiesSuiteApplication extends Application
{
    private static UtilitiesSuiteApplication instance;

    private static Animation fadeInAnimation, fadeOutAnimation;

    private static final int DATA_CHUNK_SIZE = 4096;

    public UtilitiesSuiteApplication getInstance()
    {
        return instance;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        instance = this;

        fadeInAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeInAnimation.setInterpolator(new AccelerateInterpolator());
        fadeInAnimation.setDuration(500);

        fadeOutAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        fadeOutAnimation.setInterpolator(new AccelerateInterpolator());
        fadeOutAnimation.setDuration(500);
    }

    public Animation getFadeInAnimation()
    {
        return fadeInAnimation;
    }

    public Animation getFadeOutAnimation()
    {
        return fadeOutAnimation;
    }

    public int getByteSize()
    {
        return DATA_CHUNK_SIZE;
    }
}