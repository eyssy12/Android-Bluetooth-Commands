package com.eyssyapps.bluetoothcommandsender.adapters;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.eyssyapps.bluetoothcommandsender.R;
import com.eyssyapps.bluetoothcommandsender.activities.MainActivity;
import com.eyssyapps.bluetoothcommandsender.activities.deviceinteraction.DeviceInteractionActivity;
import com.eyssyapps.bluetoothcommandsender.models.viewholders.BluetoothDeviceViewHolder;

import java.lang.reflect.Method;

/**
 * Created by eyssy on 02/07/2016.
 */
public class BluetoothDeviceAdapter extends RecyclerViewAdapterBase<BluetoothDevice, BluetoothDeviceViewHolder>
{
    private static final String THIS_CLASS_NAME = "(" + BluetoothDeviceAdapter.class.getSimpleName() + ")",
        LEFT_ARROW = " -> ",
        UNPAIR_DEVICE_TAG = THIS_CLASS_NAME + LEFT_ARROW + "DevUnpair";

    private static final String[] ITEMS = new String[] { "Connect", "Unpair", "Remove" };

    public BluetoothDeviceAdapter(Context context, View parentView)
    {
        super(context, parentView);
    }

    @Override
    public BluetoothDeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = inflater.inflate(R.layout.bluetooth_device_item, parent, false);

        return new BluetoothDeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BluetoothDeviceViewHolder holder, int position)
    {
        final BluetoothDevice item = items.get(position);

        holder.getAddress().setText(item.getAddress());
        holder.getName().setText(item.getName());
        holder.getBondState().setText(determineBondType(item.getBondState()));
        holder.getType().setText(determineDeviceType(item.getType()));

        holder.getHolderLayout().setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                AlertDialog dialog = builder
                    .setTitle("Options")
                    .setIcon(R.drawable.options)
                    .setItems(ITEMS, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            if (which == 0)
                            {
                                Intent intent = new Intent(context, DeviceInteractionActivity.class);
                                intent.putExtra(DeviceInteractionActivity.DEVICE_KEY, item);

                                Activity activityFromContext = (Activity)context;

                                activityFromContext.startActivityForResult(intent, MainActivity.REQUEST_START_DEVICE_INTERACTION);
                            }
                            else if (which == 1)
                            {
                                try
                                {
                                    Method m = item.getClass().getMethod("removeBond", (Class[]) null);

                                    m.invoke(item, (Object[]) null);
                                }
                                catch (Exception e)
                                {
                                    Log.e(UNPAIR_DEVICE_TAG, e.getMessage());
                                }
                            }
                            else if (which == 2)
                            {
                                dialog.dismiss();
                                remove(item);
                            }
                        }
                    })
                    .setNegativeButton("Dismiss", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            dialog.dismiss();
                        }
                    })
                    .create();

                dialog.show();
            }
        });
    }

    protected String determineDeviceType(int type)
    {
        switch (type)
        {
            case BluetoothDevice.DEVICE_TYPE_CLASSIC:
                return "Classic - BR/EDR device";
            case BluetoothDevice.DEVICE_TYPE_DUAL:
                return "Dual Mode - BR/EDR/LE";
            case BluetoothDevice.DEVICE_TYPE_LE:
                return "Low Energy - LE-only";
            default:
                return "Unknown";
        }
    }

    protected String determineBondType(int bondType)
    {
        switch (bondType)
        {
            case BluetoothDevice.BOND_BONDED:
                return "Paired";
            case BluetoothDevice.BOND_BONDING:
                return "Pairing";
            default:
                return "Not Paired";
        }
    }
}