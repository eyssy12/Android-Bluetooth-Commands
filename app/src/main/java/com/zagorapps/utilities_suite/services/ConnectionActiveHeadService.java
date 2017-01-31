package com.zagorapps.utilities_suite.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.facebook.rebound.BaseSpringSystem;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.zagorapps.utilities_suite.R;
import com.zagorapps.utilities_suite.activities.deviceinteraction.DeviceInteractionActivity;

/**
 * Created by eyssy on 31/01/2017.
 */

public class ConnectionActiveHeadService extends Service// implements SpringListener, SpringSystemListener
{
    private BaseSpringSystem springSystem;
    private Spring springX, springY;
    private SpringConfig config;

    private WindowManager windowManager;
    private WindowManager.LayoutParams windowLayout;

    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;

    private ImageView headView;

    private GestureDetector gestureDetector;

    @Override
    public void onCreate()
    {
        super.onCreate();

        // TODO: Investigate using Spring to make the move animation smoother
//        springSystem = SpringSystem.create();
//        springSystem.addListener(this);
//
//        springX = springSystem.createSpring();
//        springX.addListener(this);
//        springY = springSystem.createSpring();
//        springY.addListener(this);
//
//        config = new SpringConfig(5, 5);
//        springX.setSpringConfig(config);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        headView = new ImageView(this);
        headView.setImageResource(R.drawable.connection_64dp);

        gestureDetector = new GestureDetector(this, new SimpleGestureListener());

        windowLayout = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);

        windowLayout.gravity = Gravity.END | Gravity.CENTER_HORIZONTAL;

        headView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                // if clicked
                if (gestureDetector.onTouchEvent(event))
                {
                    Intent intent = new Intent(ConnectionActiveHeadService.this, DeviceInteractionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ConnectionActiveHeadService.this.startActivity(intent);

                    return true;
                }
                else // else dragged
                {
                    switch (event.getAction())
                    {
                        case MotionEvent.ACTION_DOWN:
                            initialX = windowLayout.x;
                            initialY = windowLayout.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();

                            return true;
                        case MotionEvent.ACTION_UP:
                            return true;
                        case MotionEvent.ACTION_MOVE:

//                            float offsetX = initialX + (int) (event.getRawX() - initialTouchX);
//                            float offsetY = initialY + (int) (event.getRawY() - initialTouchY);
//                            springX.setCurrentValue(springX.getCurrentValue() - offsetX).setAtRest();
//                            springY.setCurrentValue(springY.getCurrentValue() - offsetY).setAtRest();

                            // TODO: for some reason, X axis is inverted
                            windowLayout.x = -(initialX + (int) (event.getRawX() - initialTouchX));
                            windowLayout.y = initialY + (int) (event.getRawY() - initialTouchY);

//                            windowLayout.x = (int)springX.getCurrentValue();
//                            windowLayout.y = (int)springY.getCurrentValue();
                            windowManager.updateViewLayout(headView, windowLayout);
                            return true;
                    }
                    return false;
                }
            }
        });

        windowManager.addView(headView, windowLayout);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (headView != null)
        {
            windowManager.removeView(headView);
        }
    }

//    @Override
//    public void onSpringUpdate(Spring spring)
//    {
//
//    }
//
//    @Override
//    public void onSpringAtRest(Spring spring)
//    {
//
//    }
//
//    @Override
//    public void onSpringActivate(Spring spring)
//    {
//
//    }
//
//    @Override
//    public void onSpringEndStateChange(Spring spring)
//    {
//
//    }
//
//    @Override
//    public void onBeforeIntegrate(BaseSpringSystem springSystem)
//    {
//
//    }
//
//    @Override
//    public void onAfterIntegrate(BaseSpringSystem springSystem)
//    {
//
//    }

    private class SimpleGestureListener extends GestureDetector.SimpleOnGestureListener
    {
        @Override
        public boolean onSingleTapUp(MotionEvent event)
        {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent event)
        {
            return super.onDoubleTap(event);
        }
    }
}
