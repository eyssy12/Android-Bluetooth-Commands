package com.zagorapps.utilities_suite.activities.deviceinteraction;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.zagorapps.utilities_suite.R;
import com.zagorapps.utilities_suite.adapters.CopiedClipboardItemsAdapter;
import com.zagorapps.utilities_suite.custom.EmptyRecyclerView;
import com.zagorapps.utilities_suite.handlers.ServerMessageHandler;
import com.zagorapps.utilities_suite.interfaces.ServerMessagingListener;
import com.zagorapps.utilities_suite.protocol.Constants;
import com.zagorapps.utilities_suite.protocol.MessageBuilder;
import com.zagorapps.utilities_suite.services.net.ServerConnectionService;
import com.zagorapps.utilities_suite.state.ComplexPreferences;
import com.zagorapps.utilities_suite.state.models.ClipboardData;
import com.zagorapps.utilities_suite.state.models.ClipboardDataList;
import com.zagorapps.utilities_suite.utils.data.CollectionUtils;

import java.util.List;

public class ClipboardManagerActivity extends AppCompatActivity implements ServerMessagingListener
{
    private ComplexPreferences complexPreferences;
    private ServerMessageHandler messageHandler;
    private ServerConnectionService connectionService;
    private MessageBuilder messageBuilder;

    private EmptyRecyclerView emptyRecyclerView;
    private LinearLayoutManager layoutManager;
    private CopiedClipboardItemsAdapter adapter;

    private View mainContainerView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clipboard_manager);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setTitle("Saved Clipboards");

        complexPreferences = ComplexPreferences.getComplexPreferences(this, MODE_PRIVATE);
        messageBuilder = MessageBuilder.DefaultInstance();

        mainContainerView = findViewById(R.id.coordinate_layout_clipboard_manager);
        emptyRecyclerView = (EmptyRecyclerView) mainContainerView.findViewById(R.id.recycler_view_empty_support);

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        adapter = new CopiedClipboardItemsAdapter(this, emptyRecyclerView);

        emptyRecyclerView.setLayoutManager(layoutManager);
        emptyRecyclerView.setEmptyView(mainContainerView.findViewById(R.id.empty_recycler_view_state_layout));
        emptyRecyclerView.setAdapter(adapter);

        // TODO: configure for max number of saved items allowed ?
        ClipboardDataList list = complexPreferences.getObject(ClipboardDataList.class.getSimpleName(), ClipboardDataList.class);

        if (!CollectionUtils.isEmpty(list))
        {
            adapter.addCollection(list.cloneItems(), true);
        }

        this.bindToConnectionService();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        connectionService.unsubscribe(messageHandler);
        unbindService(binderService);
    }

    private void bindToConnectionService()
    {
        messageHandler = new ServerMessageHandler(this, this);

        Intent intent = new Intent(this, ServerConnectionService.class);
        bindService(intent, binderService, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection binderService = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            ServerConnectionService.ServerConnectionBinder binder = (ServerConnectionService.ServerConnectionBinder) service;

            connectionService = binder.getService();
            connectionService.subscribe(messageHandler);

//            serviceBounded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0)
        {
//            serviceBounded = false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_clipboard_manager, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_save_to_server:

                saveSelectedItemsToServer();

                return true;

            case R.id.action_clear_list:

                clearAdapter();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveSelectedItemsToServer()
    {
        List<ClipboardData> items = adapter.getSelectedItems();

        if (!CollectionUtils.isEmpty(items))
        {
            JsonObject json = MessageBuilder.DefaultInstance().getBaseObject();
            json.addProperty(Constants.KEY_IDENTIFIER, "TESTID"); // TODO: implement server side logic

            JsonArray array = new JsonArray();


            for (ClipboardData clipboard : items)
            {
                JsonObject object = new JsonObject();
                object.addProperty("created", clipboard.getDateCreatedMillis());
                object.addProperty("contents", clipboard.getContents());

                array.add(object);
            }

            json.add("data", array);

            connectionService.write(messageBuilder.toJson(json));
        }
    }

    private void clearAdapter()
    {
        if (adapter.getItemCount() > 0)
        {
            adapter.clear();

            ClipboardDataList list = complexPreferences.getObject(ClipboardDataList.class.getSimpleName(), ClipboardDataList.class);
            list.clear();

            complexPreferences.putObject(ClipboardDataList.class.getSimpleName(), list);
            complexPreferences.commit();
        }
    }

    @Override
    public void onMessageRead(String message)
    {
        // TODO: receive response from server and only then remove selected items.
    }

    @Override
    public void onMessageSent(String message)
    {
    }

    @Override
    public void onMessageFailure(String message)
    {

    }

    @Override
    public void onConnectionFailed()
    {

    }

    @Override
    public void onConnectionEstablished()
    {

    }

    @Override
    public void onConnectionAborted()
    {

    }
}
