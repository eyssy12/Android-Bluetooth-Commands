package com.zagorapps.utilities_suite.services.net;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.zagorapps.utilities_suite.Enumerations.ConnectionState;
import com.zagorapps.utilities_suite.activities.deviceinteraction.DeviceInteractionActivity;
import com.zagorapps.utilities_suite.handlers.ServerMessageHandler;
import com.zagorapps.utilities_suite.net.BluetoothConnectionThread;
import com.zagorapps.utilities_suite.state.models.BluetoothDeviceLite;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * Created by eyssy on 22/01/2017.
 */

public class ConnectionService extends Service
{
    private final IBinder binder = new ServerConnectionBinder();

    private BluetoothConnectionThread connectionThread;

    @Override
    public void onDestroy()
    {
        Log.d("Server Conn Service", "service destroyed");

        connectionThread.close();
    }

    @Override
    public void onCreate()
    {
        Log.d("Server Conn Service", "service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d("Server Conn Service", "service started");

        Bundle bundle = intent.getExtras();

        try
        {
            BluetoothDeviceLite targetDevice = bundle.getParcelable(DeviceInteractionActivity.DEVICE_KEY);
            UUID endpoint = UUID.nameUUIDFromBytes("12345".getBytes("UTF-8"));

            connectionThread = new BluetoothConnectionThread(endpoint, targetDevice);
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        return START_NOT_STICKY;
    }

    public void runConnectionThread()
    {
        if (connectionThread.getConnectionState() == ConnectionState.NOT_STARTED)
        {
            connectionThread.start();
        }
    }

    public BluetoothDeviceLite getTargetDevice()
    {
        return connectionThread.getTargetDevice();
    }

    public void write(String payload)
    {
        connectionThread.write(payload);
    }

    public void write(byte[] payload)
    {
        connectionThread.write(payload);
    }

    public boolean subscribe(ServerMessageHandler handler)
    {
        return connectionThread.subscribe(handler);
    }

    public boolean unsubscribe(ServerMessageHandler handler)
    {
        return connectionThread.unsubscribe(handler);
    }

    public boolean unsubscribe(int handlerId)
    {
        return connectionThread.unsubscribe(handlerId);
    }

    public void close()
    {
        connectionThread.close();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }

    public class ServerConnectionBinder extends Binder
    {
        public ConnectionService getService()
        {
            return ConnectionService.this;
        }
    }
}