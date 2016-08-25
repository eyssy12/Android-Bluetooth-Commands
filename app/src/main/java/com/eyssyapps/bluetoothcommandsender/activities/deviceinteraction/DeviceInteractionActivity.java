package com.eyssyapps.bluetoothcommandsender.activities.deviceinteraction;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eyssyapps.bluetoothcommandsender.R;
import com.eyssyapps.bluetoothcommandsender.enumerations.Coordinate;
import com.eyssyapps.bluetoothcommandsender.protocol.Commands;
import com.eyssyapps.bluetoothcommandsender.protocol.ServerCommands;
import com.eyssyapps.bluetoothcommandsender.state.models.BluetoothDeviceLite;
import com.eyssyapps.bluetoothcommandsender.threading.BluetoothConnectionThread;
import com.eyssyapps.bluetoothcommandsender.utils.data.TextUtils;
import com.eyssyapps.bluetoothcommandsender.utils.view.SystemMessagingUtils;
import com.eyssyapps.bluetoothcommandsender.utils.view.ViewUtils;
import com.squareup.picasso.Picasso;

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

    private Toolbar toolbar;

    private ProgressDialog progressDialog;
    private TextView textView;
    private ImageView touchpadArea;
    private Button leftClick, middleClick, rightClick;

    private BluetoothConnectionThread connectionThread;
    private BluetoothDeviceLite targetDevice;

    private GestureDetectorCompat mDetector;

    private int motionEvents = 0, eventsSent = 0;

    private float previousX, previousY;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_interaction);

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

        mDetector = new GestureDetectorCompat(this, this);
        mDetector.setOnDoubleTapListener(this);

        ViewUtils.setViewAndChildrenVisibility(findViewById(android.R.id.content), View.INVISIBLE);
        progressDialog.show();

        targetDevice = getIntent().getExtras().getParcelable(DEVICE_KEY);

        connectionThread = new BluetoothConnectionThread(SERVER_ENDPOINT, targetDevice, handler);
        connectionThread.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.device_interaction_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_interaction_settings:
                // handle
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
                eventsSent++;

                String displayInfo = "MotionEvents: " + motionEvents + "\n" + "Events sent: "+ eventsSent;
                textView.setText(displayInfo);

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

                final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinate_layout_device_interaction);
                LinearLayout linearLayout = (LinearLayout) coordinatorLayout.findViewById(R.id.content_device_interaction);
                textView = (TextView)linearLayout.findViewById(R.id.received_data_text);
                touchpadArea = (ImageView) linearLayout.findViewById(R.id.touchpad_area);

                LinearLayout btnControls = (LinearLayout)linearLayout.findViewById(R.id.btn_controls);
                leftClick = (Button)btnControls.findViewById(R.id.left_click_button);
                middleClick = (Button)btnControls.findViewById(R.id.middle_click_button);
                rightClick = (Button)btnControls.findViewById(R.id.right_click_button);

                touchpadArea.setEnabled(false);
                leftClick.setEnabled(false);
                middleClick.setEnabled(false);
                rightClick.setEnabled(false);

                Picasso.with(DeviceInteractionActivity.this).load(R.drawable.touchpad_surface).fit().into(
                        touchpadArea,
                        new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess()
                            {
                                touchpadArea.setEnabled(true);
                                leftClick.setEnabled(true);
                                middleClick.setEnabled(true);
                                rightClick.setEnabled(true);
                            }

                            @Override
                            public void onError()
                            {
                                SystemMessagingUtils.showShortToast(DeviceInteractionActivity.this, "Error loading touch pad background");
                            }
                        });

                leftClick.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        sendPayload(Commands.LEFT_CLICK.toString());
                    }
                });

                middleClick.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        sendPayload(Commands.MIDDLE_CLICK.toString());
                    }
                });

                rightClick.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        sendPayload(Commands.RIGHT_CLICK.toString());
                    }
                });

                ViewUtils.setViewAndChildrenVisibility(findViewById(android.R.id.content), View.VISIBLE);

                touchpadArea.setOnTouchListener(new View.OnTouchListener()
                {
                    @Override
                    public boolean onTouch(View v, MotionEvent event)
                    {
                        return mDetector.onTouchEvent(event);
                    }
                });

//                // temporary fix for components going off screen after being hidden or set as gone
//                simulateTouchEventForView(touchpadArea);
            }
        }
    };

    @Override
    public void onBackPressed()
    {
        // offer confirm dialog

        // TODO: ensure device can exit from activity even if the server is down

        sendPayload(Commands.END_SESSION.toString());
    }

    private void finish(int resultCode, Intent intent)
    {
        setResult(resultCode, intent);

        finish();
    }

    private float increaseMouseMovement(float movingUnits, float sensitivity, Coordinate movingUnitsType)
    {
        int min, max;
        if (movingUnitsType == Coordinate.X)
        {
            max = 1920;
            min = 0;
            
            if (movingUnits < 0)
            {
                max = 0;
                min = -1920;
            }
        }
        else 
        {
            max = 1080;
            min = 0;
            
            if (movingUnits < 0)
            {
                max = 0;
                min = -1080;
            }
        }
        
        float result = movingUnits * sensitivity;

        result = Math.max(min, result);
        result = Math.min(max, result);

        return result;
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

        motionEvents++;

//        String displayInfo = "MotionEvents: " + motionEvents + "\n" + "Events sent: "+ eventsSent;
//        touchpadArea.setText(displayInfo);

        previousX = distanceX;
        previousY = distanceY;

        if (distances.shouldSend())
        {
            String payload =
                    (increaseMouseMovement(distances.getDistanceX(), MOUSE_SENSITIVITY, Coordinate.X)) +
                    ":" +
                    (increaseMouseMovement(distances.getDistanceY(), MOUSE_SENSITIVITY, Coordinate.Y));

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

    private class MotionDistance
    {
        private float distanceX, distanceY;
        private boolean modified = false;

        public MotionDistance(float distanceX, float distanceY)
        {
            this.distanceX = distanceX;
            this.distanceY = distanceY;
        }

        public boolean isModified()
        {
            return this.modified;
        }

        public float getDistanceX()
        {
            return distanceX;
        }

        public void setDistanceX(float distanceX)
        {
            this.distanceX = distanceX;

            this.modified = true;
        }

        public float getDistanceY()
        {
            return this.distanceY;
        }

        public void setDistanceY(float distanceY)
        {
            this.distanceY = distanceY;

            this.modified = true;
        }

        private void roundX()
        {
            // Windows only allows int's as inputs hence we need to make this a whole value

            if (this.isDistanceXPositiveChange() && (this.distanceX >= 0.5 && this.distanceX <= 1.0))
            {
                this.setDistanceX(1.0f);
            }
            else if (this.distanceX <= -0.5 && this.distanceX >= -1.0)
            {
                this.setDistanceX(-1.0f);
            }
        }

        private void roundY()
        {
            // Windows only allows int's as inputs hence we need to make this a whole value

            if (this.isDistanceYPositiveChange() && (this.distanceY >= 0.5 && this.distanceY <= 1.0))
            {
                this.setDistanceY(1.0f);
            }
            else if (this.distanceY <= -0.5 && this.distanceY >= -1.0)
            {
                this.setDistanceY(-1.0f);
            }
        }

        public boolean isDistanceXPositiveChange()
        {
            return this.distanceX >= 0;
        }

        public boolean isDistanceYPositiveChange()
        {
            return this.distanceY >= 0;
        }

        public boolean shouldSend()
        {
            this.roundX();
            this.roundY();

            return this.distanceX >= 1.0 || this.distanceX <= -1.0 || this.distanceY >= 1.0 || this.distanceY <= -1.0;
        }
    }
}