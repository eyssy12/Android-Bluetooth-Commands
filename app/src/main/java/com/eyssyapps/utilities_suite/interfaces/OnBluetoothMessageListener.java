package com.eyssyapps.utilities_suite.interfaces;

/**
 * Created by eyssy on 01/09/2016.
 */
public interface OnBluetoothMessageListener
{
    void onMessageRead(String message);
    void onMessageSent(String message);
    void onMessageFailure(String message);
    void onConnectionFailed();
    void onConnectionEstablished();
    void onConnectionAborted();
}