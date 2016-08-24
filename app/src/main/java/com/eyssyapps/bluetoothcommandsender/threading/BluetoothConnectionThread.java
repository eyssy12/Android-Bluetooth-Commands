package com.eyssyapps.bluetoothcommandsender.threading;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Created by eyssy on 03/07/2016.
 */
public class BluetoothConnectionThread extends Thread
{
    public static final int MESSAGE_READ = 100,
        THREAD_ABORTED = MESSAGE_READ + 1,
        CONNECTION_ESTABLISHED = THREAD_ABORTED + 1,
        CONNECTION_FAILED = CONNECTION_ESTABLISHED + 1,
        MESSAGE_SENT = CONNECTION_FAILED + 1,
        MESSAGE_FAILED = MESSAGE_SENT + 1;

    private final UUID serviceEndpoint;
    private final Handler handler;
    private final BluetoothDevice targetDevice;

    private BluetoothSocket socket;
    private InputStream inputStream;
    private DataOutputStream writer;

    private BufferedOutputStream bufferedOutputStream;

    public BluetoothConnectionThread(UUID serviceEndpoint, BluetoothDevice targetDevice, Handler handler)
    {
        this.serviceEndpoint = serviceEndpoint;
        this.handler = handler;
        this.targetDevice = targetDevice;
    }

    public Handler getThreadHandler()
    {
        return handler;
    }

    private boolean connectionEnsured()
    {
        try
        {
            socket.connect();

            return socket.isConnected();
        }
        catch (IOException ex)
        {
            return false;
        }
    }


    public void run()
    {
        try
        {
            socket = targetDevice.createRfcommSocketToServiceRecord(serviceEndpoint);

            if (connectionEnsured())
            {
                inputStream = socket.getInputStream();
                writer = new DataOutputStream(socket.getOutputStream());
                // bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());

                // inform parent activity that the interaction with the other device is good to go
                sendHandlerMessage(CONNECTION_ESTABLISHED, 0, -1, new byte[]{});

                // Keep listening to the InputStream until an exception occurs
                while (true)
                {
                    try
                    {
                        int availableBytes = inputStream.available();
                        if (inputStream.available() > 0)
                        {
                            byte[] buffer = new byte[availableBytes];

                            // Read from the InputStream
                            int bytes = inputStream.read(buffer);

                            // Send the obtained bytes to the UI activity
                            sendHandlerMessage(MESSAGE_READ, bytes, -1, buffer);
                        }
                    }
                    catch (IOException e)
                    {
                        break;
                    }
                }
            }
            else
            {
                sendHandlerMessage(CONNECTION_FAILED, 0, -1, new byte[]{});
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void write(String data)
    {
        try
        {
            writer.writeUTF(data);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void write(byte[] data)
    {
        try
        {
            bufferedOutputStream.write(data);
            bufferedOutputStream.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void write(byte[] buffer, int offset, int count)
    {
        try
        {
            writer.write(buffer, offset, count);

            // String message = TextUtils.decodeBytesFromCharset(TextUtils.trimBytes(buffer), TextUtils.CHARSET_UTF_8);

            sendHandlerMessage(MESSAGE_SENT, count, -1, buffer);
        }
        catch (IOException e)
        {
            // broken stream/pipe

            sendHandlerMessage(MESSAGE_FAILED, 0, -1, new byte[]{});
        }
    }

    private void sendHandlerMessage(int type, int arg1, int arg2, Object obj)
    {
        Message msg = handler.obtainMessage(type, arg1, arg2, obj);
        handler.sendMessage(msg);
    }

    public void close()
    {
        try
        {
            this.closeInputStream();
            this.closeOutputStream();
            socket.close();

            sendHandlerMessage(THREAD_ABORTED, 0, -1, new byte[]{});
        }
        catch (IOException e)
        {
        }
    }

    public void closeInputStream()
    {
        try
        {
            inputStream.close();
        }
        catch (IOException e)
        {
        }
    }

    public void closeOutputStream()
    {
        try
        {
            writer.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
