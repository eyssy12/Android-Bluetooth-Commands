package com.zagorapps.utilities_suite.threading;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import com.zagorapps.utilities_suite.enumerations.ConnectionState;
import com.zagorapps.utilities_suite.state.models.BluetoothDeviceLite;
import com.zagorapps.utilities_suite.utils.data.CollectionUtils;
import com.zagorapps.utilities_suite.utils.data.TextUtils;

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
        CONNECTION_ABORTED = MESSAGE_READ + 1,
        CONNECTION_ESTABLISHED = CONNECTION_ABORTED + 1,
        CONNECTION_FAILED = CONNECTION_ESTABLISHED + 1,
        MESSAGE_SENT = CONNECTION_FAILED + 1,
        MESSAGE_FAILED = MESSAGE_SENT + 1;

    private final UUID serviceEndpoint;
    private final Handler handler;
    private final BluetoothDeviceLite targetDevice;

    private BluetoothSocket socket;
    private InputStream inputStream;
    private DataOutputStream writer;

    private ConnectionState connectionState;

    public BluetoothConnectionThread(UUID serviceEndpoint, BluetoothDeviceLite targetDevice, Handler handler)
    {
        this.serviceEndpoint = serviceEndpoint;
        this.handler = handler;
        this.targetDevice = targetDevice;

        this.connectionState = ConnectionState.NOT_STARTED;
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
            socket = targetDevice.createSocket(serviceEndpoint);

            if (connectionEnsured())
            {
                connectionState = ConnectionState.CONNECTED;

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

    // TODO: potential issue with bytes not being UTF-8
    public void write(byte[] payload)
    {
        byte[] header;

        if (payload.length <= 1024)
        {
            header = CollectionUtils.padBytes(TextUtils.getBytesForCharset(String.valueOf(payload.length), TextUtils.CHARSET_UTF_8), 4);
        }
        else
        {
            header = TextUtils.getBytesForCharset(String.valueOf(payload.length), TextUtils.CHARSET_UTF_8);
        }

        byte[] headerWithPayload = CollectionUtils.concat(header, payload);

        this.write(headerWithPayload, 0, headerWithPayload.length);
    }

    public void write(String payload)
    {
        byte[] payloadBytes = TextUtils.getBytesForCharset(payload, TextUtils.CHARSET_UTF_8);
        byte[] header;

        if (payloadBytes.length <= 1024)
        {
            header = CollectionUtils.padBytes(TextUtils.getBytesForCharset(String.valueOf(payloadBytes.length), TextUtils.CHARSET_UTF_8), 4);
        }
        else
        {
            header = TextUtils.getBytesForCharset(String.valueOf(payloadBytes.length), TextUtils.CHARSET_UTF_8);
        }

        byte[] headerWithPayload = CollectionUtils.concat(header, payloadBytes);

        this.write(headerWithPayload, 0, headerWithPayload.length);
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
            // broken stream/pipe -> most likely that the server initiated a close from its end

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

            sendHandlerMessage(CONNECTION_ABORTED, 0, -1, new byte[]{});
        }
        catch (IOException e)
        {
            // log exception
        }
        finally
        {
            connectionState = ConnectionState.DISCONNECTED;
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

    public ConnectionState getConnectionState()
    {
        return connectionState;
    }
}
