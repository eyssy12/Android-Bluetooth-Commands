package com.zagorapps.utilities_suite.services.persistent;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.zagorapps.utilities_suite.state.ComplexPreferences;
import com.zagorapps.utilities_suite.state.models.ClipboardData;
import com.zagorapps.utilities_suite.state.models.ClipboardDataList;

import org.joda.time.DateTime;

/**
 * Created by eyssy on 21/01/2017.
 */

public class ClipboardManagerListenerService extends Service
{
    private ClipboardManager manager;
    private ComplexPreferences complexPreferences;

    private ClipboardManager.OnPrimaryClipChangedListener listener = new ClipboardManager.OnPrimaryClipChangedListener()
    {
        public void onPrimaryClipChanged()
        {
            if (manager.hasPrimaryClip())
            {
                ClipboardDataList list = complexPreferences.getObject(ClipboardDataList.class.getSimpleName(), ClipboardDataList.class);

                if (list == null)
                {
                    list = new ClipboardDataList();
                }

                ClipData data = manager.getPrimaryClip();
                ClipDescription description = data.getDescription();

                // TODO: investigate whether not checking for the mime type causes unforseen side effects when retrieving the raw text clipboard data.
//                if (description.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML) || description.hasMimeType(ClipDescription.MIMETYPE_TEXT_URILIST) || description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN))
//                {
//                    ClipData.Item item = data.getItemAt(0);
//                    contents = item.getText().toString();
//                }

                ClipData.Item item = data.getItemAt(0);
                String contents = item.getText().toString();

                ClipboardData clipboard = new ClipboardData(new DateTime().getMillis(), contents);
                list.addItem(clipboard);

                complexPreferences.putObject(ClipboardDataList.class.getSimpleName(), list);
                complexPreferences.commit();
            }
        }
    };

    @Override
    public void onCreate()
    {
        manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        manager.addPrimaryClipChangedListener(listener);

        complexPreferences = ComplexPreferences.getComplexPreferences(ClipboardManagerListenerService.this, MODE_PRIVATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}
