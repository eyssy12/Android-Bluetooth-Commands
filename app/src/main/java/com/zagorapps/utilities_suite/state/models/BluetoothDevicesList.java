package com.zagorapps.utilities_suite.state.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eyssy on 24/08/2016.
 */
public class BluetoothDevicesList extends ArrayList<BluetoothDeviceLite>
{
    public BluetoothDevicesList()
    {
    }

    public BluetoothDevicesList(List<BluetoothDeviceLite> devices)
    {
        if (devices != null)
        {
            this.addAll(devices);
        }
    }

    public boolean addDevice(BluetoothDeviceLite device)
    {
        if (device == null || this.contains(device))
        {
            return false;
        }

        return this.add(device);
    }

    public boolean removeDevice(BluetoothDeviceLite device)
    {
        if (device == null || !this.contains(device))
        {
            return false;
        }

        this.remove(device);

        return true;
    }

    public List<BluetoothDeviceLite> getDevices()
    {
        return (List<BluetoothDeviceLite>)this.clone();
    }
}