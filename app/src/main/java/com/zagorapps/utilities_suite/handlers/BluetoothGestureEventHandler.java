package com.zagorapps.utilities_suite.handlers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.google.gson.JsonObject;
import com.zagorapps.utilities_suite.enumerations.ConnectionState;
import com.zagorapps.utilities_suite.enumerations.Coordinate;
import com.zagorapps.utilities_suite.interfaces.IHandler;
import com.zagorapps.utilities_suite.protocol.ClientCommands;
import com.zagorapps.utilities_suite.protocol.Constants;
import com.zagorapps.utilities_suite.protocol.MessageBuilder;
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
    private MessageBuilder messageBuilder;

    private Context context;
    private BluetoothConnectionThread connectionThread;

    private GestureDetectorCompat gestureDetector;

    private float mouseSensitivity;
    
    public BluetoothGestureEventHandler(@NonNull Context context, @NonNull BluetoothConnectionThread connectionThread, float mouseSensitivity)
    {
        this.connectionThread = connectionThread;
        this.context = context;
        this.mouseSensitivity = mouseSensitivity;

        this.messageBuilder = MessageBuilder.DefaultInstance();
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

        JsonObject object = messageBuilder.getBaseObject();
        object.addProperty(Constants.KEY_MOTION, true);
        object.addProperty(Constants.KEY_MOTION_X, MotionDistance.increaseMouseMovement(distances.getDistanceX(), 1, Coordinate.X, 1));
        object.addProperty(Constants.KEY_MOTION_Y, MotionDistance.increaseMouseMovement(distances.getDistanceY(), 1, Coordinate.Y, 1));

        connectionThread.write(messageBuilder.toJson(object));

        return true;
    }

    @Override
    public boolean onDown(MotionEvent event)
    {
        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                           float velocityX, float velocityY)
    {
        return true;
    }

    @Override
    public void onLongPress(MotionEvent event)
    {
        JsonObject object = messageBuilder.getBaseObject();
        object.addProperty(Constants.KEY_COMMAND, ClientCommands.RIGHT_CLICK.toString());

        connectionThread.write(messageBuilder.toJson(object));
    }

    @Override
    public void onShowPress(MotionEvent event)
    {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event)
    {
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event)
    {
        JsonObject object = messageBuilder.getBaseObject();
        object.addProperty(Constants.KEY_COMMAND, ClientCommands.DOUBLE_TAP.toString());

        connectionThread.write(messageBuilder.toJson(object));

        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event)
    {
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event)
    {
        JsonObject object = messageBuilder.getBaseObject();
        object.addProperty(Constants.KEY_COMMAND, ClientCommands.LEFT_CLICK.toString());

        connectionThread.write(messageBuilder.toJson(object));

        return true;
    }
}