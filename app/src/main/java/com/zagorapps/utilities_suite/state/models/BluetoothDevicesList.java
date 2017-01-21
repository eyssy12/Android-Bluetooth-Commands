package com.zagorapps.utilities_suite.state.models;

import java.util.List;

/**
 * Created by eyssy on 24/08/2016.
 */
public class BluetoothDevicesList extends CustomAbstractListBase<BluetoothDeviceLite>
{
    public BluetoothDevicesList()
    {
        super(null);
    }

    public BluetoothDevicesList(List<BluetoothDeviceLite> item)
    {
        super(item);
    }
}