package com.zagorapps.utilities_suite.models.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zagorapps.utilities_suite.R;

/**
 * Created by eyssy on 21/01/2017.
 */

public class ClipboardDataViewHolder extends RecyclerView.ViewHolder
{
    private CheckBox sendToServerCheckbox;
    private TextView timeCreated;
    private ImageButton showContentsButton;
    
    public ClipboardDataViewHolder(View itemView)
    {
        super(itemView);

        sendToServerCheckbox = (CheckBox) itemView.findViewById(R.id.checkbox_send_item); 
        timeCreated = (TextView) itemView.findViewById(R.id.text_time_copied);
        showContentsButton = (ImageButton) itemView.findViewById(R.id.imageBtn_view_clip_contents);
    }

    public View getHolderView()
    {
        return this.itemView;
    }

    public CheckBox getSendToServerCheckbox()
    {
        return sendToServerCheckbox;
    }

    public TextView getTimeCreated()
    {
        return timeCreated;
    }

    public ImageButton getShowContentsButton()
    {
        return showContentsButton;
    }
}
