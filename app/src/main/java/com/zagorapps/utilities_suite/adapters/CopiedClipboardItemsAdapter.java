package com.zagorapps.utilities_suite.adapters;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.zagorapps.utilities_suite.R;
import com.zagorapps.utilities_suite.models.viewholders.ClipboardDataViewHolder;
import com.zagorapps.utilities_suite.state.models.ClipboardData;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 * Created by eyssy on 21/01/2017.
 */

public class CopiedClipboardItemsAdapter extends RecyclerViewAdapterBase<ClipboardData, ClipboardDataViewHolder>
{
    public CopiedClipboardItemsAdapter(Context context, View parentView)
    {
        super(context, parentView);
    }

    @Override
    public ClipboardDataViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = inflater.inflate(R.layout.content_copied_clipboard_item, parent, false);

        return new ClipboardDataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ClipboardDataViewHolder holder, int position)
    {
        final ClipboardData item = items.get(position);

        holder.getSendToServerCheckbox().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {

            }
        });
        holder.getTimeCreated().setText(DateTimeFormat.mediumDateTime().print(new DateTime(item.getDateCreatedMillis())));
        holder.getShowContentsButton().setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog dialog = new AlertDialog.Builder(context)
                    .setTitle("Copied contents")
                    .setMessage(item.getContents())
                    .setCancelable(true)
                    .setPositiveButton(android.R.string.ok, null)
                    .create();

                dialog.show();
            }
        });
    }
}
