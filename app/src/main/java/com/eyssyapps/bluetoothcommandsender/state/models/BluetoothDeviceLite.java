package com.eyssyapps.bluetoothcommandsender.state.models;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;

import com.eyssyapps.bluetoothcommandsender.R;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by eyssy on 24/08/2016.
 */
public class BluetoothDeviceLite implements Parcelable
{
    protected static final Gson GSON = new Gson();

    private final String address, identity;
    private String name;
    private int bondState, deviceType;

    public static final Creator<BluetoothDeviceLite> CREATOR = new Creator<BluetoothDeviceLite>()
    {
        @Override
        public BluetoothDeviceLite createFromParcel(Parcel in)
        {
            return new BluetoothDeviceLite(in);
        }

        @Override
        public BluetoothDeviceLite[] newArray(int size)
        {
            return new BluetoothDeviceLite[size];
        }
    };

    public BluetoothDeviceLite(Resources resources, BluetoothDevice device)
    {
        if (device == null)
        {
            throw new IllegalArgumentException(resources.getString(R.string.illegal_argument_exception_bluetooth_device_missing));
        }

        this.address = device.getAddress();
        this.name = device.getName();
        this.bondState = device.getBondState();
        this.deviceType = device.getType();

        this.identity = UUID.randomUUID().toString();
    }

    public BluetoothDeviceLite(String address, String name, int bondState, int deviceType)
    {
        this.address = address;
        this.name = name;
        this.bondState = bondState;
        this.deviceType = deviceType;

        this.identity = UUID.randomUUID().toString();
    }

    protected BluetoothDeviceLite(Parcel in)
    {
        identity = in.readString();
        address = in.readString();
        name = in.readString();
        bondState = in.readInt();
        deviceType = in.readInt();
    }

    public String getAddress()
    {
        return address;
    }

    public String getName()
    {
        return name;
    }

    public int getBondState()
    {
        return bondState;
    }

    public int getDeviceType()
    {
        return deviceType;
    }

    public BluetoothSocket createSocket(UUID uuid) throws IOException
    {
        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(this.address);

        return device.createRfcommSocketToServiceRecord(uuid);
    }

    public static String determineDeviceType(int type)
    {
        switch (type)
        {
            case android.bluetooth.BluetoothDevice.DEVICE_TYPE_CLASSIC:
                return "Classic - BR/EDR device";
            case android.bluetooth.BluetoothDevice.DEVICE_TYPE_DUAL:
                return "Dual Mode - BR/EDR/LE";
            case android.bluetooth.BluetoothDevice.DEVICE_TYPE_LE:
                return "Low Energy - LE-only";
            default:
                return "Unknown";
        }
    }

    public static String determineBondType(int bondType)
    {
        switch (bondType)
        {
            case android.bluetooth.BluetoothDevice.BOND_BONDED:
                return "Paired";
            case android.bluetooth.BluetoothDevice.BOND_BONDING:
                return "Pairing";
            default:
                return "Not Paired";
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        boolean result = identity.equals(((BluetoothDeviceLite) o).identity);

        return identity.equals(((BluetoothDeviceLite) o).identity);
    }

    @Override
    public int hashCode()
    {
        int result = address.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + bondState;
        result = 31 * result + deviceType;

        return result;
    }

    @Override
    public String toString()
    {
        return "BluetoothDeviceLite{" +
                "address='" + address + '\'' +
                ", name='" + name + '\'' +
                ", bondState=" + bondState +
                ", deviceType=" + deviceType +
                '}';
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(this.identity);
        dest.writeString(this.address);
        dest.writeString(this.name);
        dest.writeInt(this.bondState);
        dest.writeInt(this.deviceType);
    }
}
