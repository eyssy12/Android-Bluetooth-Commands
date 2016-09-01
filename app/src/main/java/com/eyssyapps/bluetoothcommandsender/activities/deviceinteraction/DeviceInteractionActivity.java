package com.eyssyapps.bluetoothcommandsender.activities.deviceinteraction;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eyssyapps.bluetoothcommandsender.R;
import com.eyssyapps.bluetoothcommandsender.custom.TabbedViewPager;
import com.eyssyapps.bluetoothcommandsender.handlers.BluetoothGestureEventHandler;
import com.eyssyapps.bluetoothcommandsender.handlers.BluetoothMessageHandler;
import com.eyssyapps.bluetoothcommandsender.interfaces.OnBluetoothMessageListener;
import com.eyssyapps.bluetoothcommandsender.protocol.Commands;
import com.eyssyapps.bluetoothcommandsender.protocol.ServerCommands;
import com.eyssyapps.bluetoothcommandsender.state.InteractionTab;
import com.eyssyapps.bluetoothcommandsender.state.models.BluetoothDeviceLite;
import com.eyssyapps.bluetoothcommandsender.state.models.TabPageMetadata;
import com.eyssyapps.bluetoothcommandsender.threading.BluetoothConnectionThread;
import com.eyssyapps.bluetoothcommandsender.utils.view.ActivityUtils;
import com.eyssyapps.bluetoothcommandsender.utils.view.SystemMessagingUtils;
import com.eyssyapps.bluetoothcommandsender.utils.view.ViewUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeviceInteractionActivity extends AppCompatActivity implements
        OnBluetoothMessageListener
{
    public static final String DEVICE_KEY = "device",
        MOUSE_SENSITIVITY_KEY = "mouse_sensitivity";

    public static final int REQUEST_INTERACTION_SETTINGS = 100;

    private static final float DEFAULT_MOUSE_SENSITIVITY = (float)1.1;

    // TODO: so far, this would only connect to one pc with this designated UUID/GUID
    // if i'm going to allow for functionality to allow connection to other pc's,
    // then i will need to implement something like a barcode scanner to get the string contents for the UUID of the server rather than a manual type-in operation for the user
    private static final UUID SERVER_ENDPOINT = UUID.fromString("1f1aa577-32d6-4c59-b9a2-f262994783e9");
    private static float MOUSE_SENSITIVITY = DEFAULT_MOUSE_SENSITIVITY;

    private int keyboardInteractionViewId;
    private boolean keyboardInteractionInitiated = false;

    private View parentView;
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;
    private TabbedViewPager tabbedViewPager;
    private InteractionTab currentTab, previousTab;

    private Animation fadeInAnimation, fadeOutAnimation;
    private ProgressDialog progressDialog;
    private TextView textView;
    private ImageView touchpadArea;

    private BluetoothConnectionThread connectionThread;
    private BluetoothDeviceLite targetDevice;
    private BluetoothMessageHandler messageHandler;
    private BluetoothGestureEventHandler gestureHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_interaction);

        parentView = findViewById(android.R.id.content);

        ViewUtils.setViewAndChildrenVisibility(parentView, View.INVISIBLE);

        targetDevice = getIntent().getExtras().getParcelable(DEVICE_KEY);

        prepareStatics();
        prepareProgressDialog();
        prepareTabbedView();
        prepareBluetoothHandlers();

        gestureHandler.beginHandler();
    }

    private void prepareStatics()
    {
        fadeInAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeInAnimation.setInterpolator(new AccelerateInterpolator());
        fadeInAnimation.setDuration(300);

        fadeOutAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        fadeOutAnimation.setInterpolator(new AccelerateInterpolator());
        fadeOutAnimation.setDuration(300);
    }
    
    private void prepareProgressDialog()
    {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Connecting to device..."); // TODO: strings.xml
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
        //inflatablePageMetadata.add(new TabPageMetadata(InteractionTab.KEYBOARD, R.layout.content_device_interaction_keyboard, R.drawable.ic_keyboard_white_48dp));
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

                invalidateOptionsMenu(); //calls onCreateOptionsMenu
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

    private void prepareBluetoothHandlers()
    {
        messageHandler = new BluetoothMessageHandler(this);
        connectionThread = new BluetoothConnectionThread(SERVER_ENDPOINT, targetDevice, messageHandler);
        gestureHandler = new BluetoothGestureEventHandler(this, connectionThread, MOUSE_SENSITIVITY);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();

        fadeOutPreviousTab(menu);

        // perhaps cache the inflated menu to avoid re-inflating on view change
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
                MOUSE_SENSITIVITY = data.getFloatExtra(MOUSE_SENSITIVITY_KEY, DEFAULT_MOUSE_SENSITIVITY);

                gestureHandler.setMouseSensitivity(MOUSE_SENSITIVITY);
            }
            if (resultCode == RESULT_CANCELED)
            {
            }
        }
    }

    @Override
    public void onMessageRead(String message)
    {
        if (message.equals(ServerCommands.Close.toString()))
        {
            connectionThread.close();
        }
        else
        {
            if (!message.isEmpty())
            {
                textView.setText(message);
            }
        }
    }

    @Override
    public void onMessageSent(String message)
    {
        if (message.equals(Commands.END_SESSION.toString()))
        {
            connectionThread.close();
        }
    }

    @Override
    public void onMessageFailure(String message)
    {
        connectionThread.close();
    }

    @Override
    public void onConnectionFailed()
    {
        ActivityUtils.finish(this, RESULT_CANCELED);
    }

    @Override
    public void onConnectionEstablished()
    {
        progressDialog.dismiss();

        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initialiseTabViews();

        ViewUtils.setViewAndChildrenVisibility(parentView, View.VISIBLE);
    }

    @Override
    public void onConnectionAborted()
    {
        Intent intent = new Intent();
        intent.putExtra(DEVICE_KEY, targetDevice);

        ActivityUtils.finish(this, RESULT_OK, intent);
    }

    private void initialiseTabViews()
    {
        initialiseMouseInteractions();
        //initialiseKeyboardInteractions();
        initialiseSystemInteractions();
    }

    private void initialiseMouseInteractions()
    {
        final RelativeLayout mouseContainerView = (RelativeLayout) tabbedViewPager.getTabbedAdapter().getViewByInteractionTab(InteractionTab.MOUSE);
        final View mouseInteractionContainer = mouseContainerView.findViewById(R.id.mouse_interaction_container);
        final View keyboardInteractionInitiatorContainer = mouseContainerView.findViewById(R.id.keyboard_interaction_initiator_container);
        final LinearLayout keyboardInteractionContainer = (LinearLayout) mouseContainerView.findViewById(R.id.keyboard_interaction_container);

        textView = (TextView)mouseInteractionContainer.findViewById(R.id.received_data_text);
        touchpadArea = (ImageView) mouseInteractionContainer.findViewById(R.id.touchpad_area);
        touchpadArea.setEnabled(false);

        final ImageButton keyboardBtn = (ImageButton) keyboardInteractionInitiatorContainer.findViewById(R.id.keyboard_image_btn);

        keyboardBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // hide all the current views
                ViewUtils.setViewAndChildrenVisibility(appBarLayout, View.GONE, fadeOutAnimation);
                ViewUtils.setViewAndChildrenVisibility(mouseInteractionContainer, View.GONE, fadeOutAnimation);
                ViewUtils.setViewAndChildrenVisibility(keyboardInteractionInitiatorContainer, View.GONE, fadeOutAnimation);

                EditText keyboardTextView;
                if (keyboardInteractionInitiated)
                {
                    keyboardTextView = (EditText)keyboardInteractionContainer.findViewById(keyboardInteractionViewId);
                }
                else
                {
                    keyboardTextView = new EditText(DeviceInteractionActivity.this)
                    {
                        @Override
                        public boolean onKeyPreIme(int keyCode, KeyEvent event)
                        {
                            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
                            {
                                this.setText("");

                                ViewUtils.setViewAndChildrenVisibility(appBarLayout, View.VISIBLE, fadeInAnimation);
                                ViewUtils.setViewAndChildrenVisibility(mouseInteractionContainer, View.VISIBLE, fadeInAnimation);
                                ViewUtils.setViewAndChildrenVisibility(keyboardInteractionInitiatorContainer, View.VISIBLE, fadeInAnimation);

                                ViewUtils.setViewAndChildrenVisibility(keyboardInteractionContainer, View.GONE, fadeOutAnimation);
                            }

                            return super.onKeyPreIme(keyCode, event);
                        }
                    };

                    keyboardInteractionViewId = View.generateViewId();

                    keyboardTextView.setId(keyboardInteractionViewId);
                    keyboardTextView.setLayoutParams(new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    keyboardTextView.setHint("Type data to send");
                    keyboardTextView.setImeOptions(EditorInfo.IME_ACTION_DONE);
                    keyboardTextView.setFocusable(true);
                    keyboardTextView.setFocusableInTouchMode(true);
                    keyboardTextView.setVisibility(View.GONE);
                    keyboardTextView.addTextChangedListener(new TextWatcher()
                    {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after)
                        {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count)
                        {
                        }

                        @Override
                        public void afterTextChanged(Editable s)
                        {
                            // TODO: send payload to server
                        }
                    });

                    keyboardInteractionContainer.addView(keyboardTextView);

                    keyboardInteractionInitiated = true;
                }

                ViewUtils.setViewAndChildrenVisibility(keyboardInteractionContainer, View.VISIBLE, fadeInAnimation);

                // grab the focus and show the keyboard
                keyboardTextView.requestFocus();

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(keyboardTextView, InputMethodManager.SHOW_IMPLICIT);
            }
        });

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
                        textView.setText("Error loading touch pad background"); // TODO: strings.xml
                        SystemMessagingUtils.showShortToast(DeviceInteractionActivity.this, "Error loading touch pad background");
                    }
                });

        touchpadArea.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                return gestureHandler.onTouchEvent(event);
            }
        });
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

            connectionThread.write(Commands.END_SESSION.toString());
        }
    }
}