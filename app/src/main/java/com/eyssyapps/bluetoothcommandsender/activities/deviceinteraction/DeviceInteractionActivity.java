package com.eyssyapps.bluetoothcommandsender.activities.deviceinteraction;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.Pair;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eyssyapps.bluetoothcommandsender.R;
import com.eyssyapps.bluetoothcommandsender.custom.TabbedViewPager;
import com.eyssyapps.bluetoothcommandsender.enumerations.Coordinate;
import com.eyssyapps.bluetoothcommandsender.protocol.Commands;
import com.eyssyapps.bluetoothcommandsender.protocol.ServerCommands;
import com.eyssyapps.bluetoothcommandsender.state.InteractionTab;
import com.eyssyapps.bluetoothcommandsender.state.models.BluetoothDeviceLite;
import com.eyssyapps.bluetoothcommandsender.state.models.MotionDistance;
import com.eyssyapps.bluetoothcommandsender.state.models.TabPageMetadata;
import com.eyssyapps.bluetoothcommandsender.threading.BluetoothConnectionThread;
import com.eyssyapps.bluetoothcommandsender.utils.data.TextUtils;
import com.eyssyapps.bluetoothcommandsender.utils.view.SystemMessagingUtils;
import com.eyssyapps.bluetoothcommandsender.utils.view.ViewUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeviceInteractionActivity extends AppCompatActivity implements
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener
{
    public static final String DEVICE_KEY = "device",
        MOUSE_SENSITIVITY_KEY = "mouse_sensitivity";

    public static final int REQUEST_INTERACTION_SETTINGS = 100;
    private static final String DEBUG_TAG = "Gestures";
    private static final float DEFAULT_MOUSE_SENSITIVITY = (float)1.1;

    private static final UUID SERVER_ENDPOINT = UUID.fromString("1f1aa577-32d6-4c59-b9a2-f262994783e9");
    private static float MOUSE_SENSITIVITY = DEFAULT_MOUSE_SENSITIVITY;

    private View parentView;
    private Toolbar toolbar;
    private TabbedViewPager tabbedViewPager;
    private InteractionTab currentTab, previousTab;

    private Animation fadeInAnimation, fadeOutAnimation;
    private ProgressDialog progressDialog;
    private TextView textView;
    private ImageView touchpadArea;

    private BluetoothConnectionThread connectionThread;
    private BluetoothDeviceLite targetDevice;

    private GestureDetectorCompat mDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_interaction);

        parentView = findViewById(android.R.id.content);

        ViewUtils.setViewAndChildrenVisibility(parentView, View.INVISIBLE);

        prepareStatics();

        targetDevice = getIntent().getExtras().getParcelable(DEVICE_KEY);
        mDetector = new GestureDetectorCompat(this, this);
        mDetector.setOnDoubleTapListener(this);

        prepareProgressDialog();
        prepareTabbedView();

        connectionThread = new BluetoothConnectionThread(SERVER_ENDPOINT, targetDevice, handler);
        connectionThread.start();
    }

    private void prepareStatics()
    {
        fadeInAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeInAnimation.setInterpolator(new AccelerateInterpolator());
        fadeInAnimation.setDuration(150);

        fadeOutAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        fadeOutAnimation.setInterpolator(new AccelerateInterpolator());
        fadeOutAnimation.setDuration(150);
    }
    
    private void prepareProgressDialog()
    {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Connecting to device...");
        progressDialog.setTitle("Operation in progress");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                setResult(RESULT_FIRST_USER);

                finish();
            }
        });
        progressDialog.show();
    }

    private void prepareTabbedView()
    {
        Pair<Integer, Integer> viewPagerTabLayoutResIds = new Pair<>(
            R.id.device_interaction_view_pager,
            R.id.device_interaction_tabs);
        
        List<TabPageMetadata> inflatablePageMetadata = new ArrayList<>();
        inflatablePageMetadata.add(new TabPageMetadata(InteractionTab.MOUSE, R.layout.content_device_interaction_mouse, R.drawable.ic_mouse_white_48dp));
        inflatablePageMetadata.add(new TabPageMetadata(InteractionTab.KEYBOARD, R.layout.content_device_interaction_keyboard, R.drawable.ic_keyboard_white_48dp));
        inflatablePageMetadata.add(new TabPageMetadata(InteractionTab.SYSTEM, R.layout.content_device_interaction_system, R.drawable.ic_assignment_white_48dp));

        ViewPager.OnPageChangeListener changeListener = new ViewPager.OnPageChangeListener()
        {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
                // TODO: could try and see if the pageScrolled is over 50% of the tab and then fade out any menu items
            }

            @Override
            public void onPageSelected(int position)
            {
                previousTab = currentTab;
                currentTab = InteractionTab.getEnumFromOrder(position);

                invalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {

            }
        };

        tabbedViewPager = new TabbedViewPager(this, parentView, changeListener, viewPagerTabLayoutResIds, inflatablePageMetadata);

        currentTab = tabbedViewPager.getDefaultTab();
        previousTab = InteractionTab.NOT_SET;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();

        fadeOutPreviousTab(menu);

        switch (currentTab)
        {
            case MOUSE:

                inflater.inflate(R.menu.device_interaction_mouse_menu, menu);

                // TODO: figure out animations
//                MenuItem item;
//                item = menu.findItem(R.id.action_interaction_settings);
//                item.setActionView(R.layout.action_settings);
//                item.getActionView().startAnimation(fadeInAnimation);

            case KEYBOARD:
                inflater.inflate(R.menu.device_interaction_keyboard_menu, menu);
            case SYSTEM:
                inflater.inflate(R.menu.device_interaction_system_menu, menu);
        }

        return true;
    }

    private void fadeOutPreviousTab(Menu menu)
    {
        switch (previousTab)
        {
            case MOUSE:
            case KEYBOARD:

            case SYSTEM:
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_interaction_settings:

                Intent intent = new Intent(this, InteractionSettingsActivity.class);
                intent.putExtra(MOUSE_SENSITIVITY_KEY, MOUSE_SENSITIVITY);
                startActivityForResult(intent, REQUEST_INTERACTION_SETTINGS);

                return true;
            default:

                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_INTERACTION_SETTINGS)
        {
            if (resultCode == RESULT_OK)
            {
                float newSensitivity = data.getFloatExtra(MOUSE_SENSITIVITY_KEY, DEFAULT_MOUSE_SENSITIVITY);
                MOUSE_SENSITIVITY = newSensitivity;
            }
            if (resultCode == RESULT_CANCELED)
            {
            }
        }
    }

    private final Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            String data = new String((byte[])msg.obj);
            data = data.trim();

            if (msg.what == BluetoothConnectionThread.MESSAGE_READ)
            {
                if (data.equals(ServerCommands.Close.toString()))
                {
                    connectionThread.close();
                }
                else
                {
                    if (!data.isEmpty())
                    {
                        textView.setText(data);
                    }
                }
            }
            else if (msg.what == BluetoothConnectionThread.MESSAGE_SENT)
            {
                if (data.equals(Commands.END_SESSION.toString()))
                {
                    connectionThread.close();
                }
            }
            else if (msg.what == BluetoothConnectionThread.MESSAGE_FAILED)
            {
                connectionThread.close();
            }
            else if (msg.what == BluetoothConnectionThread.THREAD_ABORTED)
            {
                Intent intent = new Intent();
                intent.putExtra(DEVICE_KEY, targetDevice);

                finish(RESULT_OK, intent);
            }
            else if (msg.what == BluetoothConnectionThread.CONNECTION_FAILED)
            {
                Intent intent = new Intent();
                finish(RESULT_CANCELED, intent);
            }
            else if (msg.what == BluetoothConnectionThread.CONNECTION_ESTABLISHED)
            {
                progressDialog.dismiss();

                toolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                initialiseTabViews();

                ViewUtils.setViewAndChildrenVisibility(findViewById(android.R.id.content), View.VISIBLE);
            }
        }
    };

    private void initialiseTabViews()
    {
        initialiseMouseInteractions();
        initialiseKeyboardInteractions();
        initialiseSystemInteractions();
    }

    private void initialiseMouseInteractions()
    {
        View mouseContainerView = tabbedViewPager.getTabbedAdapter().getViewByInteractionTab(InteractionTab.MOUSE);

        LinearLayout linearLayout = (LinearLayout) mouseContainerView.findViewById(R.id.content_device_interaction_mouse);
        textView = (TextView)linearLayout.findViewById(R.id.received_data_text);
        touchpadArea = (ImageView) linearLayout.findViewById(R.id.touchpad_area);

        touchpadArea.setEnabled(false);

        Picasso.with(DeviceInteractionActivity.this)
               .load(R.drawable.touchpad_surface)
               .fit()
               .into(touchpadArea, new com.squareup.picasso.Callback()
               {
                    @Override
                    public void onSuccess()
                    {
                        touchpadArea.setEnabled(true);
                    }

                    @Override
                    public void onError()
                    {
                        textView.setText("Error loading touch pad background");
                        SystemMessagingUtils.showShortToast(DeviceInteractionActivity.this, "Error loading touch pad background");
                    }
                });

        touchpadArea.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                return mDetector.onTouchEvent(event);
            }
        });

        // temporary fix for components going off screen after being hidden or set as gone
        // simulateTouchEventForView(touchpadArea);
    }

    private void initialiseKeyboardInteractions()
    {
        View keyboardContainerView = tabbedViewPager.getTabbedAdapter().getViewByInteractionTab(InteractionTab.KEYBOARD);
    }

    private void initialiseSystemInteractions()
    {
        View systemContainerView = tabbedViewPager.getTabbedAdapter().getViewByInteractionTab(InteractionTab.SYSTEM);
    }

    @Override
    public void onBackPressed()
    {
        if (currentTab != tabbedViewPager.getDefaultTab())
        {
            tabbedViewPager.setCurrentTab(tabbedViewPager.getDefaultTab());
        }
        else
        {
            // TODO: offer confirm dialog

            sendPayload(Commands.END_SESSION.toString());
        }
    }

    private void finish(int resultCode, Intent intent)
    {
        setResult(resultCode, intent);

        finish();
    }



    private void sendPayload(String payload)
    {
        byte[] bytes = TextUtils.getBytesForUtf8(payload, 64);

        // TODO: figure out the 'packet' sizes, header cannot consist of a single byte to indicate the data length - need 4-8 bytes for that
//        byte[] payloadBytes = getBytesForCharset(payload, "UTF-8");
//        byte[] bytes = new byte[payloadBytes.length + 1];
//
//        bytes[0] = String.valueOf(payloadBytes.length).getBytes()[0];
//
//        for (int i = 0; i < payloadBytes.length; i++)
//        {
//            bytes[i + 1] = payloadBytes[i];
//        }

        connectionThread.write(bytes, 0, bytes.length);
    }

    @Override
    public boolean onDown(MotionEvent event)
    {
        Log.d(DEBUG_TAG,"onDown: " + event.toString());

        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                           float velocityX, float velocityY)
    {
        Log.d(DEBUG_TAG, "onFling: " + event1.toString()+event2.toString());

        return true;
    }

    @Override
    public void onLongPress(MotionEvent event)
    {
        Log.d(DEBUG_TAG, "onLongPress: " + event.toString());
        sendPayload(Commands.RIGHT_CLICK.toString());
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
    {
        MotionDistance distances = new MotionDistance(-distanceX, -distanceY);

        if (distances.shouldSend())
        {
            String payload =
                    (MotionDistance.increaseMouseMovement(distances.getDistanceX(), MOUSE_SENSITIVITY, Coordinate.X)) +
                    ":" +
                    (MotionDistance.increaseMouseMovement(distances.getDistanceY(), MOUSE_SENSITIVITY, Coordinate.Y));

            sendPayload(payload);
        }

        return true;
    }

    @Override
    public void onShowPress(MotionEvent event)
    {
        Log.d(DEBUG_TAG, "onShowPress: " + event.toString());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event)
    {
        Log.d(DEBUG_TAG, "onSingleTapUp: " + event.toString());

        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event)
    {
        Log.d(DEBUG_TAG, "onDoubleTap: " + event.toString());
        sendPayload(Commands.DOUBLE_TAP.toString());

        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event)
    {
        Log.d(DEBUG_TAG, "onDoubleTapEvent: " + event.toString());

        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event)
    {
        Log.d(DEBUG_TAG, "onSingleTapConfirmed: " + event.toString());
        sendPayload(Commands.LEFT_CLICK.toString());

        return true;
    }
}