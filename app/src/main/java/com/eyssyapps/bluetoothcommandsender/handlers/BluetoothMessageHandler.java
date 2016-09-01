package com.eyssyapps.bluetoothcommandsender.handlers;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;

import com.eyssyapps.bluetoothcommandsender.interfaces.OnBluetoothMessageListener;
import com.eyssyapps.bluetoothcommandsender.threading.BluetoothConnectionThread;

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
        String data = new String((byte[])msg.obj);
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
