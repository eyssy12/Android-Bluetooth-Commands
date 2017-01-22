package com.zagorapps.utilities_suite.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.zagorapps.utilities_suite.R;
import com.zagorapps.utilities_suite.activities.MainActivity;
import com.zagorapps.utilities_suite.activities.deviceinteraction.DeviceInteractionActivity;
import com.zagorapps.utilities_suite.models.viewholders.BluetoothDeviceViewHolder;
import com.zagorapps.utilities_suite.state.models.BluetoothDeviceLite;

import java.lang.reflect.Method;

/**
 * Created by eyssy on 02/07/2016.
 */
public class BluetoothDeviceAdapter extends RecyclerViewAdapterBase<BluetoothDeviceLite, BluetoothDeviceViewHolder>
{
    private static final String THIS_CLASS_NAME = "(" + BluetoothDeviceAdapter.class.getSimpleName() + ")", LEFT_ARROW = " -> ", UNPAIR_DEVICE_TAG = THIS_CLASS_NAME + LEFT_ARROW + "DevUnpair";

    private static final String[] ITEMS = new String[]{"Connect", "Remove from list", "Unpair"};

    public BluetoothDeviceAdapter(Context context, View parentView)
    {
        super(context, parentView);
    }

    @Override
    public BluetoothDeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = inflater.inflate(R.layout.content_bluetooth_device_item, parent, false);

        return new BluetoothDeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BluetoothDeviceViewHolder holder, int position)
    {
        final BluetoothDeviceLite item = items.get(position);

        holder.getAddress().setText(item.getAddress());
        holder.getName().setText(item.getName());
        holder.getBondState().setText(BluetoothDeviceLite.determineBondType(item.getBondState()));
        holder.getType().setText(BluetoothDeviceLite.determineDeviceType(item.getDeviceType()));

        holder.getHolderLayout().setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                AlertDialog dialog = builder.setTitle("Options").setIcon(R.drawable.ic_toc_black).setItems(ITEMS, new DialogInterface.OnClickListener()
                {
                    // TODO: change this to setAdapter instead to customise the dialog better with icons
                    // display image buttons for "Connect", "Remove from list"
                    // the custom view will have to be inflated using the R.layout.{id} and then added via the addView method of the dialog.

                    public void onClick(DialogInterface dialog, int which)
                    {
                        if (which == 0)
                        {
                            Intent intent = new Intent(context, DeviceInteractionActivity.class);
                            intent.putExtra(DeviceInteractionActivity.DEVICE_KEY, item);

                            Activity activityFromContext = (Activity) context;

                            activityFromContext.startActivityForResult(intent, MainActivity.REQUEST_START_DEVICE_INTERACTION);
                        }
                        else if (which == 2)
                        {
                            try
                            {
                                // TODO: how to invoke unpairing on both sides - should it even be part of the actions ?
                                // as it is now, it only unpairs from this side, the server side still considers this device to be paired
                                Method m = item.getClass().getMethod("removeBond", (Class[]) null);

                                m.invoke(item, (Object[]) null);
                            }
                            catch (Exception e)
                            {
                                Log.e(UNPAIR_DEVICE_TAG, e.getMessage());
                            }
                        }
                        else if (which == 1)
                        {
                            dialog.dismiss();
                            remove(item);
                        }
                    }
                }).setNegativeButton("Dismiss", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.dismiss();
                    }
                }).create();

                dialog.show();
            }
        });
    }
}