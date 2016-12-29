package com.zagorapps.utilities_suite.activities.prototypes;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.zagorapps.utilities_suite.R;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UdpConnectToServer extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_udp_connect_to_server);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        View parentView = this.findViewById(R.id.coordinate_layout_udpserver);

        final EditText input = (EditText) parentView.findViewById(R.id.userInput);

        Button button = (Button)parentView.findViewById(R.id.sendDataButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                new AsyncTask<String, Void, Void>()
                {
                    @Override
                    protected Void doInBackground(String... params)
                    {
                        if (params[0].length() > 0)
                        {
                            try
                            {
                                DatagramSocket clientSocket = new DatagramSocket();
                                InetAddress test = InetAddress.getLocalHost();
                                InetAddress IPAddress = InetAddress.getByName("192.168.1.1");
                                byte[] sendData = new byte[1024];
                                sendData = params[0].getBytes();
                                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 30301);
                                clientSocket.send(sendPacket);

                                clientSocket.close();
                            }
                            catch (SocketException e)
                            {
                                e.printStackTrace();
                            }
                            catch (UnknownHostException e)
                            {
                                e.printStackTrace();
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        }

                        return null;
                    }
                }.execute(input.getText().toString());


            }
        });
    }

}
