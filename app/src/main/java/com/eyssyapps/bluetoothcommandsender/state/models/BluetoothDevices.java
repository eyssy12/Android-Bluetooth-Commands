package com.eyssyapps.bluetoothcommandsender.state.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eyssy on 24/08/2016.
 */
public class BluetoothDevices
{
    private List<BluetoothDeviceLite> devices;

    public BluetoothDevices(List<BluetoothDeviceLite> devices)
    {
        if (devices == null)
        {
            this.devices = new ArrayList<>();
        }

        this.devices = devices;
    }

    public boolean add(BluetoothDeviceLite device)
    {
        if (device == null || devices.contains(device))
        {
            return false;
        }

        return this.devices.add(device);
    }

    public boolean remove(BluetoothDeviceLite device)
    {
        if (device == null || !devices.contains(device))
        {
            return false;
        }

        this.devices.remove(device);

        return true;
    }

    public void clear()
    {
        this.devices.clear();
    }

    public List<BluetoothDeviceLite> getDevices()
    {
        return devices;
    }
}