package com.zagorapps.utilities_suite.handlers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.zagorapps.utilities_suite.enumerations.ConnectionState;
import com.zagorapps.utilities_suite.enumerations.Coordinate;
import com.zagorapps.utilities_suite.interfaces.IHandler;
import com.zagorapps.utilities_suite.protocol.Commands;
import com.zagorapps.utilities_suite.state.models.MotionDistance;
import com.zagorapps.utilities_suite.threading.BluetoothConnectionThread;

/**
 * Created by eyssy on 01/09/2016.
 */
public class BluetoothGestureEventHandler implements
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener,
        IHandler
{
    private static final String DEBUG_TAG = "BluetoothGestureHandler";

    private Context context;
    private BluetoothConnectionThread connectionThread;

    private GestureDetectorCompat gestureDetector;

    private float mouseSensitivity;
    
    public BluetoothGestureEventHandler(@NonNull Context context, @NonNull BluetoothConnectionThread connectionThread, float mouseSensitivity)
    {
        this.connectionThread = connectionThread;
        this.context = context;
        this.mouseSensitivity = mouseSensitivity;
    }

    public void setMouseSensitivity(float sensitivity)
    {
        if (sensitivity < 0.1)
        {
            this.mouseSensitivity = 0.1f;
        }
        
        this.mouseSensitivity = sensitivity;
    }

    public boolean onTouchEvent(MotionEvent event)
    {
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public void beginHandler()
    {
        this.gestureDetector = new GestureDetectorCompat(context, this);
        this.gestureDetector.setOnDoubleTapListener(this);

        if (connectionThread.getConnectionState() == ConnectionState.NOT_STARTED)
        {
            connectionThread.start();
        }
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
    {
        MotionDistance distances = new MotionDistance(-distanceX, -distanceY); // 2 decimal places

//        if (distances.shouldSend())
//        {
//
//        }

        String payload =
                (MotionDistance.increaseMouseMovement(distances.getDistanceX(), 1, Coordinate.X, 1)) +
                ":" +
                (MotionDistance.increaseMouseMovement(distances.getDistanceY(), 1, Coordinate.Y, 1));

        connectionThread.write(payload);

        return true;
    }

    @Override
    public boolean onDown(MotionEvent event)
    {
        Log.d(DEBUG_TAG,"onDown: " + event.toString());

        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                           float velocityX, float velocityY)
    {
        Log.d(DEBUG_TAG, "onFling: " + event1.toString()+event2.toString());

        return true;
    }

    @Override
    public void onLongPress(MotionEvent event)
    {
        Log.d(DEBUG_TAG, "onLongPress: " + event.toString());
        connectionThread.write(Commands.RIGHT_CLICK.toString());
    }

    @Override
    public void onShowPress(MotionEvent event)
    {
        Log.d(DEBUG_TAG, "onShowPress: " + event.toString());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event)
    {
        Log.d(DEBUG_TAG, "onSingleTapUp: " + event.toString());

        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event)
    {
        Log.d(DEBUG_TAG, "onDoubleTap: " + event.toString());
        connectionThread.write(Commands.DOUBLE_TAP.toString());

        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event)
    {
        Log.d(DEBUG_TAG, "onDoubleTapEvent: " + event.toString());

        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event)
    {
        Log.d(DEBUG_TAG, "onSingleTapConfirmed: " + event.toString());
        connectionThread.write(Commands.LEFT_CLICK.toString());

        return true;
    }
}