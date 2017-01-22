package com.zagorapps.utilities_suite.handlers;

import android.app.Activity;
import android.os.Message;
import android.support.annotation.NonNull;

import com.zagorapps.utilities_suite.interfaces.ServerMessagingListener;
import com.zagorapps.utilities_suite.threading.BluetoothConnectionThread;

/**
 * Created by eyssy on 01/09/2016.
 */
public class ServerMessageHandler extends HandlerBase
{
    private Activity context;
    private ServerMessagingListener messageListener;

    public ServerMessageHandler(@NonNull Activity context, @NonNull ServerMessagingListener messageListener)
    {
        super();

        this.context = context;
        this.messageListener = messageListener;
    }

    @Override
    public void handleMessage(final Message msg)
    {
        final String data = new String((byte[]) msg.obj).trim();

        context.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
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
        });
    }
}
