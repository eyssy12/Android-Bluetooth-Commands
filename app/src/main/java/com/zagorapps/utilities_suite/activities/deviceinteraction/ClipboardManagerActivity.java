package com.zagorapps.utilities_suite.activities.deviceinteraction;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.zagorapps.utilities_suite.R;
import com.zagorapps.utilities_suite.adapters.CopiedClipboardItemsAdapter;
import com.zagorapps.utilities_suite.custom.EmptyRecyclerView;
import com.zagorapps.utilities_suite.state.ComplexPreferences;
import com.zagorapps.utilities_suite.state.models.ClipboardDataList;
import com.zagorapps.utilities_suite.utils.data.CollectionUtils;

public class ClipboardManagerActivity extends AppCompatActivity
{
    private ComplexPreferences complexPreferences;

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
    }

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
        // TODO: need to have the bluetooth connection thread as a service...

        // get all checked items and package them to JSON
        // send data to server as a "file" action
        // if successful, remove from items from adapter and preferences
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
}
