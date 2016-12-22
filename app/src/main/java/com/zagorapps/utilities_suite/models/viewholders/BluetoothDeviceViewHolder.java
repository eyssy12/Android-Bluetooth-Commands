package com.zagorapps.utilities_suite.models.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.zagorapps.utilities_suite.R;

/**
 * Created by eyssy on 02/07/2016.
 */
public class BluetoothDeviceViewHolder extends RecyclerView.ViewHolder
{
    private View holder;
    private TextView name, address, type, bondState;

    public BluetoothDeviceViewHolder(View itemView)
    {
        super(itemView);

        // TODO: finish view
        holder = itemView.findViewById(R.id.device_item_layout);
        name = (TextView) itemView.findViewById(R.id.device_name);

        View addressContainer = itemView.findViewById(R.id.device_address_container_layout);
        address = (TextView) addressContainer.findViewById(R.id.device_address);

        View typeContainer = itemView.findViewById(R.id.device_type_container_layout);
        type = (TextView) typeContainer.findViewById(R.id.device_type);

        View bondStateContainer = itemView.findViewById(R.id.device_bond_state_container_layout);
        bondState = (TextView) bondStateContainer.findViewById(R.id.device_bond_state);
    }

    public View getHolderLayout()
    {
        return holder;
    }

    public TextView getName()
    {
        return name;
    }

    public TextView getAddress()
    {
        return address;
    }

    public TextView getType()
    {
        return type;
    }

    public TextView getBondState()
    {
        return bondState;
    }
}