package com.zagorapps.utilities_suite.handlers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.google.gson.JsonObject;
import com.zagorapps.utilities_suite.Enumerations.Coordinate;
import com.zagorapps.utilities_suite.interfaces.SimpleHandler;
import com.zagorapps.utilities_suite.protocol.ClientCommands;
import com.zagorapps.utilities_suite.protocol.Constants;
import com.zagorapps.utilities_suite.protocol.MessageBuilder;
import com.zagorapps.utilities_suite.services.net.ConnectionService;
import com.zagorapps.utilities_suite.state.models.MotionDistance;

/**
 * Created by eyssy on 01/09/2016.
 */
public class GestureEventHandler implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, SimpleHandler
{
    private MessageBuilder messageBuilder;

    private Context context;
    private ConnectionService connectionService;

    private GestureDetectorCompat gestureDetector;

    private float mouseSensitivity;
    private boolean serviceBounded = false;
    
    public GestureEventHandler(@NonNull Context context, float mouseSensitivity)
    {
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

    public void stopHandler()
    {
        if (serviceBounded)
        {
            context.unbindService(binderService);
        }
    }
    
    @Override
    public void beginHandler()
    {
        if (!serviceBounded)
        {
            gestureDetector = new GestureDetectorCompat(context, this);
            gestureDetector.setOnDoubleTapListener(this);

            Intent intent = new Intent(context, ConnectionService.class);
            context.bindService(intent, binderService, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
    {
        MotionDistance distances = new MotionDistance(-distanceX, -distanceY); // 2 decimal places

        JsonObject object = messageBuilder.getBaseObject();
        object.addProperty(Constants.KEY_IDENTIFIER, Constants.KEY_MOTION);
        object.addProperty(Constants.KEY_MOTION_X, MotionDistance.increaseMouseMovement(distances.getDistanceX(), 1, Coordinate.X, 1));
        object.addProperty(Constants.KEY_MOTION_Y, MotionDistance.increaseMouseMovement(distances.getDistanceY(), 1, Coordinate.Y, 1));

        connectionService.write(messageBuilder.toJson(object));

        return true;
    }

    @Override
    public boolean onDown(MotionEvent event)
    {
        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY)
    {
        return true;
    }

    @Override
    public void onLongPress(MotionEvent event)
    {
        JsonObject object = messageBuilder.getBaseObject();
        object.addProperty(Constants.KEY_IDENTIFIER, Constants.KEY_COMMAND);
        object.addProperty(Constants.KEY_VALUE, ClientCommands.RIGHT_CLICK.toString());

        connectionService.write(messageBuilder.toJson(object));
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
        object.addProperty(Constants.KEY_IDENTIFIER, Constants.KEY_COMMAND);
        object.addProperty(Constants.KEY_VALUE, ClientCommands.DOUBLE_TAP.toString());

        connectionService.write(messageBuilder.toJson(object));

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
        object.addProperty(Constants.KEY_IDENTIFIER, Constants.KEY_COMMAND);
        object.addProperty(Constants.KEY_VALUE, ClientCommands.LEFT_CLICK.toString());

        connectionService.write(messageBuilder.toJson(object));

        return true;
    }

    private ServiceConnection binderService = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ConnectionService.ServerConnectionBinder binder = (ConnectionService.ServerConnectionBinder) service;
            connectionService = binder.getService();

            serviceBounded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0)
        {
            stopHandler();

            serviceBounded = false;
        }
    };
}