package com.eyssyapps.bluetoothcommandsender.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.eyssyapps.bluetoothcommandsender.R;
import com.eyssyapps.bluetoothcommandsender.activities.deviceinteraction.DeviceInteractionActivity;
import com.eyssyapps.bluetoothcommandsender.adapters.BluetoothDeviceAdapter;
import com.eyssyapps.bluetoothcommandsender.custom.EmptyRecyclerView;
import com.eyssyapps.bluetoothcommandsender.state.ComplexPreferences;
import com.eyssyapps.bluetoothcommandsender.state.models.BluetoothDeviceLite;
import com.eyssyapps.bluetoothcommandsender.state.models.BluetoothDevices;
import com.eyssyapps.bluetoothcommandsender.utils.view.SystemMessagingUtils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    public static final int REQUEST_ENABLE_BT = 100,
        REQUEST_CONNECTION_FAILED_TO_DEVICE = REQUEST_ENABLE_BT + 1,
        REQUEST_START_DEVICE_INTERACTION = REQUEST_CONNECTION_FAILED_TO_DEVICE + 1;

    private static String TITLE_FORMAT;

    private EmptyRecyclerView emptyRecyclerView;
    private LinearLayoutManager layoutManager;
    private BluetoothDeviceAdapter adapter;
    private ComplexPreferences complexPreferences;

    private Toolbar toolbar;
    private View mainContainerView;
    private View contentContainerView;
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

        prepareStatics();

        mainContainerView = findViewById(R.id.coordinate_layout_main);
        contentContainerView = mainContainerView.findViewById(R.id.content_main_include);

        // TODO: create a TabPager that has a tab for discovering devices, another for already paired devices

        toolbarProgressBar = (ProgressBar) mainContainerView.findViewById(R.id.toolbar_progress_bar);
        toolbarProgressBar.setIndeterminate(true);
        toolbarProgressBar.setVisibility(View.INVISIBLE);

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        prepareAdapter();
        prepareDiscoveryButton();
        prepareBluetoothAdapter();
    }

    private void prepareStatics()
    {
        TITLE_FORMAT = getString(R.string.format_device_discovery_title);
        complexPreferences = ComplexPreferences.getComplexPreferences(this, "mypref", MODE_PRIVATE);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        resources = getResources();
    }

    private void prepareDiscoveryButton()
    {
        btnDiscover = (Button) contentContainerView.findViewById(R.id.btn_discovery);
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

    private void prepareBluetoothAdapter()
    {
        if (bluetoothAdapter == null)
        {
            btnDiscover.setText(resources.getString(R.string.bluetooth_unsupported_device_message));
            btnDiscover.setEnabled(false);
        }
        else
        {
            registerReceiver(broadcastReceiver, getBluetoothIntentFilter()); // Don't forget to unregister during onDestroy

            if (!bluetoothAdapter.isEnabled())
            {
                btnDiscover.setText(resources.getString(R.string.enable_bluetooth));
            }
        }
    }

    private IntentFilter getBluetoothIntentFilter()
    {
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

        return filter;
    }

    private void prepareAdapter()
    {
        emptyRecyclerView = (EmptyRecyclerView)contentContainerView.findViewById(R.id.recycler_view_empty_support);
        emptyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new BluetoothDeviceAdapter(this, emptyRecyclerView);

        BluetoothDevices devices = complexPreferences.getObject(BluetoothDevices.class.getSimpleName(), BluetoothDevices.class);
        if (devices != null)
        {
            adapter.replaceCollection(devices.getDevices(), true);
        }
        else
        {
            devices = new BluetoothDevices(new ArrayList<BluetoothDeviceLite>());

            complexPreferences.putObject(BluetoothDevices.class.getSimpleName(), devices);
            complexPreferences.commit();
        }

        emptyRecyclerView.setEmptyView(contentContainerView.findViewById(R.id.empty_recycler_view_state_layout));
        emptyRecyclerView.setAdapter(adapter);
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

                BluetoothDevices devices = complexPreferences.getObject(BluetoothDevices.class.getSimpleName(), BluetoothDevices.class);
                BluetoothDeviceLite device = data.getExtras().getParcelable(DeviceInteractionActivity.DEVICE_KEY);

                if (devices.add(device))
                {
                    complexPreferences.putObject(BluetoothDevices.class.getSimpleName(), devices);
                    complexPreferences.commit();
                }
            }
            else if (resultCode == RESULT_CANCELED)
            {
                SystemMessagingUtils.showShortToast(this, "Unable to connect to selected device.");
            }
        }
    }

    @Override
    public void onPause()
    {
        unregisterReceiver(broadcastReceiver);

        super.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        registerReceiver(broadcastReceiver, getBluetoothIntentFilter());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_clear_preferences:

                complexPreferences.clear();
                SystemMessagingUtils.showSnackBar(mainContainerView, "Preferences Cleared!", Snackbar.LENGTH_SHORT);

                return true;
            case R.id.action_clear_bonded_devices:

                BluetoothDevices devices = complexPreferences.getObject(BluetoothDevices.class.getSimpleName(), BluetoothDevices.class);
                devices.clear();

                complexPreferences.putObject(BluetoothDevices.class.getSimpleName(), devices);
                complexPreferences.commit();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                adapter.addLast(new BluetoothDeviceLite(resources, device));

                String text = String.format(TITLE_FORMAT, adapter.getItemCount(), resources.getString(R.string.discovery_activity_discovering_message));
                toolbar.setTitle(text);
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                toolbarProgressBar.setVisibility(View.INVISIBLE);

                String text = String.format(TITLE_FORMAT, adapter.getItemCount(), resources.getString(R.string.discovery_activity_default_title));
                toolbar.setTitle(text);

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