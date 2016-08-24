package com.eyssyapps.bluetoothcommandsender.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.eyssyapps.bluetoothcommandsender.R;
import com.eyssyapps.bluetoothcommandsender.adapters.BluetoothDeviceAdapter;
import com.eyssyapps.bluetoothcommandsender.custom.EmptyRecyclerView;
import com.eyssyapps.bluetoothcommandsender.utils.view.SystemMessagingUtils;

public class MainActivity extends AppCompatActivity
{
    public static final int REQUEST_ENABLE_BT = 100,
        REQUEST_CONNECTION_FAILED_TO_DEVICE = REQUEST_ENABLE_BT + 1,
        REQUEST_START_DEVICE_INTERACTION = REQUEST_CONNECTION_FAILED_TO_DEVICE + 1;

    private EmptyRecyclerView emptyRecyclerView;
    private LinearLayoutManager layoutManager;
    private BluetoothDeviceAdapter adapter;

    private Toolbar toolbar;
    private Button btnDiscover;
    private BluetoothAdapter bluetoothAdapter;
    private ProgressBar toolbarProgressBar;
    private Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        resources = getResources();

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinate_layout_main);
        RelativeLayout relativeLayout = (RelativeLayout) coordinatorLayout.findViewById(R.id.content_main_include);

        // TODO: create a TabPager that has a tab for discovering devices, another for already paired devices

        toolbarProgressBar = (ProgressBar) coordinatorLayout.findViewById(R.id.toolbar_progress_bar);
        toolbarProgressBar.setIndeterminate(true);
        toolbarProgressBar.setVisibility(View.INVISIBLE);

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        emptyRecyclerView = (EmptyRecyclerView)relativeLayout.findViewById(R.id.recycler_view_empty_support);
        emptyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BluetoothDeviceAdapter(this, emptyRecyclerView);

        emptyRecyclerView.setEmptyView(relativeLayout.findViewById(R.id.empty_recycler_view_state_layout));
        emptyRecyclerView.setAdapter(adapter);

        btnDiscover = (Button) relativeLayout.findViewById(R.id.btn_discovery);

        if (bluetoothAdapter == null)
        {
            btnDiscover.setText(resources.getString(R.string.bluetooth_unsupported_device_message));
            btnDiscover.setEnabled(false);
        }
        else
        {
            // Register the BroadcastReceiver
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

            if (!bluetoothAdapter.isEnabled())
            {
                btnDiscover.setText(resources.getString(R.string.enable_bluetooth));
            }
        }

        btnDiscover.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (bluetoothAdapter.isEnabled())
                {
                    if (bluetoothAdapter.isDiscovering())
                    {
                        toolbar.setTitle(resources.getString(R.string.discovery_activity_default_title));

                        bluetoothAdapter.cancelDiscovery();
                        btnDiscover.setText(resources.getString(R.string.do_device_discovery));
                        toolbarProgressBar.setVisibility(View.INVISIBLE);
                    }
                    else
                    {
                        adapter.clear();

                        toolbar.setTitle(resources.getString(R.string.discovery_activity_discovering_message));
                        bluetoothAdapter.startDiscovery();
                        btnDiscover.setText(resources.getString(R.string.cancel_device_discovery));
                        toolbarProgressBar.setVisibility(View.VISIBLE);
                    }
                }
                else
                {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_ENABLE_BT)
        {
            if (resultCode == RESULT_OK)
            {
                btnDiscover.setText(resources.getString(R.string.do_device_discovery));
            }
            else if (resultCode == RESULT_CANCELED)
            {
                btnDiscover.setText(resources.getString(R.string.enable_bluetooth));
            }
        }
        else if (requestCode == REQUEST_START_DEVICE_INTERACTION)
        {
            if (resultCode == RESULT_OK)
            {
                SystemMessagingUtils.showShortToast(this, "Connection successfully closed!");
            }
            else if (resultCode == RESULT_CANCELED)
            {
                SystemMessagingUtils.showShortToast(this, "Unable to connect to selected device.");
            }
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                adapter.add(device);

                toolbar.setTitle("(" + adapter.getItemCount() + ") " + resources.getString(R.string.discovery_activity_discovering_message));
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                toolbarProgressBar.setVisibility(View.INVISIBLE);
                toolbar.setTitle("(" + adapter.getItemCount() + ") " + resources.getString(R.string.discovery_activity_default_title));
                btnDiscover.setText(resources.getString(R.string.do_device_discovery));
            }
            else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED))
            {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch (state)
                {
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        btnDiscover.setText(resources.getString(R.string.enable_bluetooth));
                        break;
                }
            }
        }
    };
}
