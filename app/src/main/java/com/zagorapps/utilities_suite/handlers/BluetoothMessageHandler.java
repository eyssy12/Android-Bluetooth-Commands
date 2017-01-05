package com.zagorapps.utilities_suite.handlers;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;

import com.zagorapps.utilities_suite.interfaces.OnBluetoothMessageListener;
import com.zagorapps.utilities_suite.threading.BluetoothConnectionThread;

/**
 * Created by eyssy on 01/09/2016.
 */
public class BluetoothMessageHandler extends Handler
{
    private OnBluetoothMessageListener messageListener;

    public BluetoothMessageHandler(@NonNull OnBluetoothMessageListener messageListener)
    {
        this.messageListener = messageListener;
    }

    @Override
    public void handleMessage(Message msg)
    {
        String data = new String((byte[]) msg.obj);
        data = data.trim();

        if (msg.what == BluetoothConnectionThread.MESSAGE_READ)
        {
            messageListener.onMessageRead(data);
        }
        else if (msg.what == BluetoothConnectionThread.MESSAGE_SENT)
        {
            messageListener.onMessageSent(data);
        }
        else if (msg.what == BluetoothConnectionThread.MESSAGE_FAILED)
        {
            messageListener.onMessageFailure(data);
        }
        else if (msg.what == BluetoothConnectionThread.CONNECTION_ABORTED)
        {
            messageListener.onConnectionAborted();
        }
        else if (msg.what == BluetoothConnectionThread.CONNECTION_FAILED)
        {
            messageListener.onConnectionFailed();
        }
        else if (msg.what == BluetoothConnectionThread.CONNECTION_ESTABLISHED)
        {
            messageListener.onConnectionEstablished();
        }
    }
}
