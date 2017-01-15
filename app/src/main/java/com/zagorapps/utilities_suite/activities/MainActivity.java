package com.zagorapps.utilities_suite.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.zagorapps.utilities_suite.R;
import com.zagorapps.utilities_suite.activities.deviceinteraction.DeviceInteractionActivity;
import com.zagorapps.utilities_suite.adapters.BluetoothDeviceAdapter;
import com.zagorapps.utilities_suite.custom.EmptyRecyclerView;
import com.zagorapps.utilities_suite.state.ComplexPreferences;
import com.zagorapps.utilities_suite.state.models.BluetoothDeviceLite;
import com.zagorapps.utilities_suite.state.models.BluetoothDevicesList;
import com.zagorapps.utilities_suite.utils.view.ActivityUtils;
import com.zagorapps.utilities_suite.utils.view.SystemMessagingUtils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    public static final int REQUEST_ENABLE_BT = 100, REQUEST_CONNECTION_FAILED_TO_DEVICE = REQUEST_ENABLE_BT + 1, REQUEST_START_DEVICE_INTERACTION = REQUEST_CONNECTION_FAILED_TO_DEVICE + 1, REQUEST_QR_SCANNER = REQUEST_START_DEVICE_INTERACTION + 1;

    private static final int PERMISSION_REQUEST_CAMERA = 200;

    private static String TITLE_FORMAT;

    private EmptyRecyclerView emptyRecyclerView;
    private LinearLayoutManager layoutManager;
    private BluetoothDeviceAdapter adapter;
    private ComplexPreferences complexPreferences;

    private Toolbar toolbar;
    private Menu optionsMenu;
    private View mainContainerView;
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

        // TODO: use this when notification logic will be implementedd
        // ShortcutBadger.applyCount(getApplicationContext(), 2);

        mainContainerView = findViewById(R.id.coordinate_layout_main);

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
        btnDiscover = (Button) mainContainerView.findViewById(R.id.btn_discovery);
        btnDiscover.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MenuItem item = optionsMenu.findItem(R.id.action_connect_using_qr_code);

                if (bluetoothAdapter.isEnabled())
                {
                    item.setVisible(true);

                    if (bluetoothAdapter.isDiscovering())
                    {
                        toolbar.setTitle(resources.getString(R.string.title_discovery_activity_default));

                        bluetoothAdapter.cancelDiscovery();
                        btnDiscover.setText(resources.getString(R.string.action_discover_devices));
                        toolbarProgressBar.setVisibility(View.INVISIBLE);
                    }
                    else
                    {
                        adapter.clear();

                        toolbar.setTitle(resources.getString(R.string.message_discovery_activity_discovering));
                        bluetoothAdapter.startDiscovery();
                        btnDiscover.setText(resources.getString(R.string.action_cancel_device_discovery));
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
            btnDiscover.setText(resources.getString(R.string.message_bluetooth_unsupported));
            btnDiscover.setEnabled(false);
        }
        else
        {
            registerReceiver(broadcastReceiver, getBluetoothIntentFilter()); // Don't forget to unregister during onDestroy

            if (!bluetoothAdapter.isEnabled())
            {
                btnDiscover.setText(resources.getString(R.string.action_enable_bluetooth));
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
        emptyRecyclerView = (EmptyRecyclerView) mainContainerView.findViewById(R.id.recycler_view_empty_support);
        emptyRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new BluetoothDeviceAdapter(this, emptyRecyclerView);

        BluetoothDevicesList devices = complexPreferences.getObject(BluetoothDevicesList.class.getSimpleName(), BluetoothDevicesList.class);
        if (devices == null)
        {
            devices = new BluetoothDevicesList(new ArrayList<BluetoothDeviceLite>());

            complexPreferences.putObject(BluetoothDevicesList.class.getSimpleName(), devices);
            complexPreferences.commit();
        }
        else
        {
            adapter.replaceCollection(devices.getDevices(), true);
        }

        emptyRecyclerView.setEmptyView(mainContainerView.findViewById(R.id.empty_recycler_view_state_layout));
        emptyRecyclerView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_ENABLE_BT)
        {
            MenuItem item = optionsMenu.findItem(R.id.action_connect_using_qr_code);

            if (resultCode == RESULT_OK)
            {
                item.setVisible(true);
                btnDiscover.setText(resources.getString(R.string.action_discover_devices));
            }
            else if (resultCode == RESULT_CANCELED)
            {
                item.setVisible(false);
                btnDiscover.setText(resources.getString(R.string.action_enable_bluetooth));
            }
        }
        else if (requestCode == REQUEST_QR_SCANNER)
        {
            if (resultCode == RESULT_OK)
            {
                String[] split = data.getStringExtra("qr_result").split("-");

                BluetoothDeviceLite device = new BluetoothDeviceLite(split[1], split[0]);

                Intent intent = new Intent(this, DeviceInteractionActivity.class);
                intent.putExtra(DeviceInteractionActivity.DEVICE_KEY, device);

                this.startActivityForResult(intent, MainActivity.REQUEST_START_DEVICE_INTERACTION);
            }
        }
        else if (requestCode == REQUEST_START_DEVICE_INTERACTION)
        {
            if (resultCode == RESULT_OK)
            {
                SystemMessagingUtils.showShortToast(this, "Connection successfully closed!");

                BluetoothDevicesList devices = complexPreferences.getObject(BluetoothDevicesList.class.getSimpleName(), BluetoothDevicesList.class);
                BluetoothDeviceLite device = data.getExtras().getParcelable(DeviceInteractionActivity.DEVICE_KEY);

                if (devices == null)
                {
                    devices = new BluetoothDevicesList();
                }

                if (devices.addDevice(device))
                {
                    complexPreferences.putObject(BluetoothDevicesList.class.getSimpleName(), devices);
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
        this.optionsMenu = menu;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.action_connect_using_qr_code);
        if (bluetoothAdapter.isEnabled())
        {
            item.setVisible(true);
        }
        else
        {
            item.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_connect_using_qr_code:

                requestQrCodeScanner();

                return true;
            case R.id.action_clear_preferences:

                complexPreferences.clear();
                SystemMessagingUtils.showSnackBar(mainContainerView, "Preferences Cleared!", Snackbar.LENGTH_SHORT);

                return true;
            case R.id.action_clear_bonded_devices:

                BluetoothDevicesList devices = complexPreferences.getObject(BluetoothDevicesList.class.getSimpleName(), BluetoothDevicesList.class);
                devices.clear();

                complexPreferences.putObject(BluetoothDevicesList.class.getSimpleName(), devices);
                complexPreferences.commit();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case PERMISSION_REQUEST_CAMERA:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    ActivityUtils.simpleStartActivityForResult(this, QrCodeScannerActivity.class, REQUEST_QR_SCANNER);
                }
                else
                {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }
        }
    }

    private void requestQrCodeScanner()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA))
            {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            }
            else
            {
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            }
        }
        else
        {
            ActivityUtils.simpleStartActivityForResult(this, QrCodeScannerActivity.class, REQUEST_QR_SCANNER);
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

                String text = String.format(TITLE_FORMAT, adapter.getItemCount(), resources.getString(R.string.message_discovery_activity_discovering));
                toolbar.setTitle(text);
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                toolbarProgressBar.setVisibility(View.INVISIBLE);

                String text = String.format(TITLE_FORMAT, adapter.getItemCount(), resources.getString(R.string.title_discovery_activity_default));
                toolbar.setTitle(text);

                btnDiscover.setText(resources.getString(R.string.action_discover_devices));
            }
            else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED))
            {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                MenuItem item = optionsMenu.findItem(R.id.action_connect_using_qr_code);

                switch (state)
                {
                    case BluetoothAdapter.STATE_TURNING_OFF:

                        item.setVisible(false);
                        btnDiscover.setText(resources.getString(R.string.action_enable_bluetooth));
                        break;

                    case BluetoothAdapter.STATE_TURNING_ON:

                        btnDiscover.setText(R.string.message_bluetooth_turning_on);
                        break;

                    case BluetoothAdapter.STATE_ON:

                        item.setVisible(true);
                        btnDiscover.setText(R.string.action_discover_devices);
                        break;
                }
            }
        }
    };
}