package com.zagorapps.utilities_suite.activities.deviceinteraction;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.design.widget.AppBarLayout;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.gson.JsonObject;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.squareup.picasso.Picasso;
import com.zagorapps.utilities_suite.R;
import com.zagorapps.utilities_suite.UtilitiesSuiteApplication;
import com.zagorapps.utilities_suite.custom.TabbedViewPager;
import com.zagorapps.utilities_suite.handlers.GestureEventHandler;
import com.zagorapps.utilities_suite.handlers.ServerMessageHandler;
import com.zagorapps.utilities_suite.interfaces.FileSenderListener;
import com.zagorapps.utilities_suite.interfaces.ServerMessagingListener;
import com.zagorapps.utilities_suite.managers.FileSenderManager;
import com.zagorapps.utilities_suite.models.QueuedFile;
import com.zagorapps.utilities_suite.protocol.ClientCommands;
import com.zagorapps.utilities_suite.protocol.Constants;
import com.zagorapps.utilities_suite.protocol.MessageBuilder;
import com.zagorapps.utilities_suite.protocol.ServerCommands;
import com.zagorapps.utilities_suite.services.HeadConnectionService;
import com.zagorapps.utilities_suite.services.net.ConnectionService;
import com.zagorapps.utilities_suite.state.InteractionTab;
import com.zagorapps.utilities_suite.state.models.TabPageMetadata;
import com.zagorapps.utilities_suite.utils.data.CollectionUtils;
import com.zagorapps.utilities_suite.utils.data.NumberUtils;
import com.zagorapps.utilities_suite.utils.threading.RunnableUtils;
import com.zagorapps.utilities_suite.utils.view.ActivityUtils;
import com.zagorapps.utilities_suite.utils.view.SystemMessagingUtils;
import com.zagorapps.utilities_suite.utils.view.ViewUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeviceInteractionActivity extends AppCompatActivity implements ServerMessagingListener, FileSenderListener
{
    public static final String DEVICE_KEY = "device", MOUSE_SENSITIVITY_KEY = "mouse_sensitivity";

    public static final int REQUEST_INTERACTION_SETTINGS = 100,
        REQUEST_FILE_PICKER = REQUEST_INTERACTION_SETTINGS + 1,
        REQUEST_CLIPBOARD_MANAGER = REQUEST_FILE_PICKER + 1;

    private static final int ID_FILE_SEND_NOTIFICATION = 100;

    private static final float DEFAULT_MOUSE_SENSITIVITY = (float) 1.1;

    private static UUID SERVER_ENDPOINT = UUID.fromString("1f1aa577-32d6-4c59-b9a2-f262994783e9");
    private static float MOUSE_SENSITIVITY = DEFAULT_MOUSE_SENSITIVITY;

    private int keyboardInteractionViewId;
    private boolean keyboardInteractionInitiated = false;
    private boolean doubleBackToExitPressedOnce = false;

    // Parent View
    private View parentView;
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;
    private TabbedViewPager tabbedViewPager;
    private InteractionTab currentTab, previousTab;

    // Mouse View
    private ProgressDialog progressDialog;
    private TextView textView;
    private ImageView touchPadArea;

    // Voice View
    private TextView txtSpeechInput;
    private ImageButton btnSpeak;
    private SpeechRecognizer speechRecogniser;
    private Intent speechRecogniserIntent;
    private boolean isSpeechRecogniserListening = false;
    private AlertDialog machineLockedDialog;

    // System View
    private SeekBar systemVolumeSeekBar;
    private SeekBar.OnSeekBarChangeListener systemVolumeSeekBarChangeListener;

    private ToggleButton muteButton;
    private ToggleButtonCheckedChangeListener muteButtonCheckedChangeListener;
    
    private SeekBar screenBrightnessSeekBar;
    private SeekBar.OnSeekBarChangeListener screenBrightnessSeekBarChangeListener;

    private AlertDialog.Builder systemControlDialogBuilder;

    private FileSenderManager fileSenderManager;

    // Specifics
    private UtilitiesSuiteApplication application;
    private NotificationManager notifyManager;
    private NotificationCompat.Builder fileSendNotificationBuilder;

    private ServerMessageHandler messageHandler;
    private GestureEventHandler gestureHandler;

    private ConnectionService connectionService;
    private boolean serviceBounded = false;

    private String[] initialSyncData;
    private boolean serverSyncUpInitialised = false;

    private IntentFilter deviceChargeStateFilter;

    private MessageBuilder messageBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_interaction);

        parentView = findViewById(android.R.id.content);

        ViewUtils.setViewAndChildrenVisibility(parentView, View.INVISIBLE);

        prepareStatics();
        prepareMachineAlertDialog();
        prepareProgressDialog();
        prepareSystemControlDialog();
        prepareTabbedView();
        prepareHandlers();
        prepareVoiceRecogniser();

        beginInteraction(getIntent().getExtras());
    }

    private void beginInteraction(Bundle extras)
    {
        Intent intent = new Intent(this, ConnectionService.class);
        intent.putExtras(extras); // pass on the target device bundle to service

        startService(intent);
        bindService(intent, binderService, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection binderService = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            ConnectionService.ServerConnectionBinder binder = (ConnectionService.ServerConnectionBinder) service;

            connectionService = binder.getService();
            connectionService.subscribe(messageHandler);

            gestureHandler.beginHandler();
            connectionService.runConnectionThread();

            serviceBounded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0)
        {
            serviceBounded = false;
        }
    };

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

    @Override
    public void onBackPressed()
    {
        if (currentTab != tabbedViewPager.getDefaultTab())
        {
            tabbedViewPager.setCurrentTab(tabbedViewPager.getDefaultTab());
        }
        else
        {
            if (doubleBackToExitPressedOnce)
            {
                JsonObject object = messageBuilder.getBaseObject();
                object.addProperty(Constants.KEY_IDENTIFIER, Constants.KEY_COMMAND);
                object.addProperty(Constants.KEY_VALUE, ClientCommands.END_SESSION.toString());

                connectionService.write(messageBuilder.toJson(object));
            }
            else
            {
                this.doubleBackToExitPressedOnce = true;

                SystemMessagingUtils.showToast(this, "Please click BACK again to exit", Toast.LENGTH_SHORT);

                RunnableUtils.executeWithDelay(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);
            }
        }
    }

    @Override
    public void onPause()
    {
        unregisterReceiver(batteryStateReceiver);

        super.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        registerReceiver(batteryStateReceiver, deviceChargeStateFilter);
    }

    @Override
    public void onMessageRead(String message)
    {
        // TODO: implement JSON based messaging
        if (message.equals(ServerCommands.CLOSE.toString()))
        {
            gestureHandler.stopHandler();

            unbindService(binderService);
            stopService(new Intent(this, ConnectionService.class));
        }
        else if (message.equals("Prohibited Process Running"))
        {
            SystemMessagingUtils.showShortToast(this, "Unable to perform system command since more than one prohibited process is running.");
        }
        else if (message.equals("machine_locked"))
        {
            machineLockedDialog.show();
        }
        else if (message.equals("machine_unlocked"))
        {
            machineLockedDialog.dismiss();
        }
        else if (message.contains(":"))
        {
            String[] data = message.split(":");

            if (data[0].equals(Constants.VALUE_SYNC_RESPONSE))
            {
                startService(new Intent(this, HeadConnectionService.class));

                // TODO: return all data for the sync operation

                JsonObject object = messageBuilder.getBaseObject();
                object.addProperty(Constants.KEY_IDENTIFIER, Constants.KEY_SYNC_STATE);
                object.addProperty(Constants.KEY_VALUE, Constants.VALUE_SYNC_RESPONSE_ACK);

                connectionService.write(messageBuilder.toJson(object));

                if (!serverSyncUpInitialised)
                {
                    appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
                    toolbar = (Toolbar) findViewById(R.id.toolbar);

                    setSupportActionBar(toolbar);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                    initialSyncData = data[1].split("_");

                    initialiseTabViews();

                    progressDialog.dismiss();

                    serverSyncUpInitialised = true;

                    ViewUtils.setViewAndChildrenVisibility(parentView, View.VISIBLE);
                }
            }
            else if (data[0].equals("screen"))
            {
                int value = Integer.valueOf(data[1]);

                screenBrightnessSeekBar.setOnSeekBarChangeListener(null);
                screenBrightnessSeekBar.setProgress(value);
                screenBrightnessSeekBar.setOnSeekBarChangeListener(screenBrightnessSeekBarChangeListener);
            }
            else if (data[0].equals("file"))
            {
                String[] split = data[1].split("_");
                String responseKey = split[1];

                if (responseKey.equals("readyForSending"))
                {
                    fileSenderManager.beginQueuedFileSend();
                }
                else if (responseKey.equals("chunkAccepted"))
                {
                    fileSenderManager.prepareNextAvailable();
                }
                else if (responseKey.equals("finished"))
                {
                    fileSenderManager.finalizeQueuedFile();
                }
            }
            else if (data[0].equals("vol"))
            {
                // Data sent by the server is not one that the listeners should relay back to it, only user interaction ones
                if (NumberUtils.isNumeric(data[1]))
                {
                    Double value = Double.valueOf(data[1]);

                    if (muteButton.isChecked())
                    {
                        this.setMuteButtonChecked(false);
                    }

                    systemVolumeSeekBar.setOnSeekBarChangeListener(null);
                    systemVolumeSeekBar.setProgress(value.intValue());
                    systemVolumeSeekBar.setOnSeekBarChangeListener(systemVolumeSeekBarChangeListener);
                }
                else
                {
                    boolean value = Boolean.valueOf(data[1]);

                    this.setMuteButtonChecked(value);
                }
            }
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
        if (message.equals(ClientCommands.END_SESSION.toString()))
        {
            connectionService.close();
        }
    }

    @Override
    public void onMessageFailure(String message)
    {
        connectionService.close();
    }

    @Override
    public void onConnectionFailed()
    {
        stopService(new Intent(this, HeadConnectionService.class));

        ActivityUtils.finish(this, RESULT_CANCELED);
    }

    @Override
    public void onConnectionEstablished()
    {
        JsonObject object = messageBuilder.getBaseObject();
        object.addProperty(Constants.KEY_IDENTIFIER, Constants.KEY_SYNC_STATE);
        object.addProperty(Constants.KEY_VALUE, Constants.VALUE_SYNC_REQUEST);

        connectionService.write(messageBuilder.toJson(object));
    }

    @Override
    public void onConnectionAborted()
    {
        stopService(new Intent(this, HeadConnectionService.class));

        Intent intent = new Intent();
        intent.putExtra(DEVICE_KEY, connectionService.getTargetDevice());

        ActivityUtils.finish(this, RESULT_OK, intent);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if (speechRecogniser != null)
        {
            speechRecogniser.destroy();
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
        }
        else if (requestCode == REQUEST_FILE_PICKER)
        {
            if (resultCode == RESULT_OK)
            {
                ArrayList<String> paths = data.getStringArrayListExtra(FilePickerActivity.EXTRA_PATHS);

                if (!CollectionUtils.isEmpty(paths))
                {
                    for (String path : paths)
                    {
                        File file = com.nononsenseapps.filepicker.Utils.getFileForUri(Uri.parse(path));

                        fileSenderManager.enqueue(file);
                    }
                }
            }
        }
    }

    private void prepareVoiceRecogniser()
    {
        speechRecogniserIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecogniserIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecogniserIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());

        speechRecogniser = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecogniser.setRecognitionListener(new SpeechRecognitionListener());
    }

    private void prepareStatics()
    {
        application = (UtilitiesSuiteApplication) getApplicationContext();
        notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        fileSendNotificationBuilder = new NotificationCompat.Builder(this);
        fileSendNotificationBuilder.setContentTitle("File upload")
                .setContentText("Upload in progress")
                .setSmallIcon(R.drawable.ic_file_upload);

        messageBuilder = MessageBuilder.DefaultInstance();

        deviceChargeStateFilter = new IntentFilter();
        deviceChargeStateFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        deviceChargeStateFilter.addAction(Intent.ACTION_BATTERY_LOW);
        deviceChargeStateFilter.addAction(Intent.ACTION_BATTERY_OKAY);

        registerReceiver(batteryStateReceiver, deviceChargeStateFilter);
    }

    private void prepareMachineAlertDialog()
    {
        this.machineLockedDialog = new AlertDialog.Builder(this).setTitle("Machine Locked").setIcon(R.drawable.ic_phonelink_lock).setCancelable(false).create();

        // TODO: this is not working, should investigate
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(machineLockedDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;

        machineLockedDialog.getWindow().setAttributes(lp);
    }

    private void prepareProgressDialog()
    {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Connecting to device..."); // TODO: strings.xml
        progressDialog.setTitle("Operation in progress");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                ActivityUtils.finish(DeviceInteractionActivity.this, RESULT_FIRST_USER);
            }
        });
        progressDialog.show();
    }

    private void prepareSystemControlDialog()
    {
        systemControlDialogBuilder = new AlertDialog.Builder(DeviceInteractionActivity.this);
        systemControlDialogBuilder.setTitle("Confirm Action")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                    }
                })
                .setCancelable(true);
    }

    private void prepareTabbedView()
    {
        Pair<Integer, Integer> viewPagerTabLayoutResIds = new Pair<>(R.id.device_interaction_view_pager, R.id.device_interaction_tabs);

        List<TabPageMetadata> inflatablePageMetadata = new ArrayList<>();
        inflatablePageMetadata.add(new TabPageMetadata(InteractionTab.MOUSE, R.layout.content_device_interaction_mouse, R.drawable.ic_mouse));
        inflatablePageMetadata.add(new TabPageMetadata(InteractionTab.KEYBOARD, R.layout.content_device_interaction_keyboard, R.drawable.ic_keyboard_white));
        inflatablePageMetadata.add(new TabPageMetadata(InteractionTab.SYSTEM, R.layout.content_device_interaction_system, R.drawable.ic_assignment_white));
        inflatablePageMetadata.add(new TabPageMetadata(InteractionTab.VOICE, R.layout.content_device_interaction_voice, R.drawable.ic_record_voice_over_white));

        ViewPager.OnPageChangeListener changeListener = new ViewPager.OnPageChangeListener()
        {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
            }

            @Override
            public void onPageSelected(int position)
            {
                previousTab = currentTab;
                currentTab = InteractionTab.getEnumFromOrder(position);

                if (currentTab == InteractionTab.MOUSE)
                {
                    tabbedViewPager.disablePageListener();
                }
                else
                {
                    tabbedViewPager.enablePageListener();
                }

                doubleBackToExitPressedOnce = false;

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

    private void prepareHandlers()
    {
        messageHandler = new ServerMessageHandler(this, this);
        gestureHandler = new GestureEventHandler(this, MOUSE_SENSITIVITY);
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

    private void initialiseTabViews()
    {
        initialiseMouseAndKeyboardInteractions();
        initialiseSpecialKeyboardInteractions();
        initialiseSystemInteractions();
        initialiseVoiceInteractions();
    }

    private void initialiseMouseAndKeyboardInteractions()
    {
        final RelativeLayout mouseContainerView = (RelativeLayout) tabbedViewPager.getTabbedAdapter().getViewByInteractionTab(InteractionTab.MOUSE);
        final View mouseInteractionContainer = mouseContainerView.findViewById(R.id.mouse_interaction_container);
        final View keyboardInteractionInitiatorContainer = mouseContainerView.findViewById(R.id.keyboard_interaction_initiator_container);
        final LinearLayout keyboardInteractionContainer = (LinearLayout) mouseContainerView.findViewById(R.id.keyboard_interaction_container);

        this.textView = (TextView) mouseInteractionContainer.findViewById(R.id.received_data_text);
        this.touchPadArea = (ImageView) mouseInteractionContainer.findViewById(R.id.touchpad_area);
        this.touchPadArea.setEnabled(false);

        final ImageButton keyboardBtn = (ImageButton) keyboardInteractionInitiatorContainer.findViewById(R.id.keyboard_image_btn);

        final List<View> mouseTouchPadInteractiveViews = new ArrayList<>();
        mouseTouchPadInteractiveViews.add(this.appBarLayout);
        mouseTouchPadInteractiveViews.add(mouseInteractionContainer);
        mouseTouchPadInteractiveViews.add(keyboardInteractionInitiatorContainer);

        // TODO: this should display a view that is "included" in the parent view of the interaction container
        // <android:include />

        // I shouldn't need to programatically create textview's etc.
        keyboardBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // hide all the current views
                ViewUtils.setViewAndChildrenVisibility(mouseTouchPadInteractiveViews, View.GONE, application.getFadeOutAnimation());

                final TextView characterDisplayView;
                if (keyboardInteractionInitiated)
                {
                    characterDisplayView = (TextView) keyboardInteractionContainer.findViewById(keyboardInteractionViewId);
                }
                else
                {
                    characterDisplayView = new TextView(DeviceInteractionActivity.this)
                    {
                        @Override
                        public boolean onKeyPreIme(int keyCode, KeyEvent event)
                        {
                            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
                            {
                                // the Soft keyboard has a delay when it is disposed hence it doesnt look as nice when the other views get rendered before it
                                RunnableUtils.executeWithDelay(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        ViewUtils.setViewAndChildrenVisibility(mouseTouchPadInteractiveViews, View.VISIBLE, application.getFadeInAnimation());

                                        ViewUtils.setViewAndChildrenVisibility(keyboardInteractionContainer, View.GONE, application.getFadeOutAnimation());
                                    }
                                }, 250);
                            }

                            return super.onKeyPreIme(keyCode, event);
                        }
                    };

                    keyboardInteractionViewId = View.generateViewId();

                    characterDisplayView.setId(keyboardInteractionViewId);
                    characterDisplayView.setTextSize(24);
                    characterDisplayView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    characterDisplayView.setLayoutParams(new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    characterDisplayView.setHint("Type data to send");
                    characterDisplayView.setImeOptions(EditorInfo.IME_ACTION_DONE);
                    characterDisplayView.setFocusable(true);
                    characterDisplayView.setFocusableInTouchMode(true);
                    characterDisplayView.setVisibility(View.GONE);
                    characterDisplayView.setOnKeyListener(new View.OnKeyListener()
                    {
                        @Override
                        public boolean onKey(View v, int keyCode, KeyEvent event)
                        {
                            if (event.getAction() == KeyEvent.ACTION_UP)
                            {
                                String sendValue;

                                if (keyCode == KeyEvent.KEYCODE_DEL)
                                {
                                    sendValue = ClientCommands.BACKSPACE.toString();
                                }
                                else
                                {
                                    sendValue = String.valueOf((char) event.getUnicodeChar());
                                }

                                characterDisplayView.setText(sendValue);

                                JsonObject object = messageBuilder.getBaseObject();
                                object.addProperty(Constants.KEY_IDENTIFIER, Constants.KEY_COMMAND);
                                object.addProperty(Constants.KEY_VALUE, sendValue);

                                connectionService.write(messageBuilder.toJson(object));
                            }

                            return true;
                        }
                    });

                    keyboardInteractionContainer.addView(characterDisplayView);

                    keyboardInteractionInitiated = true;
                }

                ViewUtils.setViewAndChildrenVisibility(keyboardInteractionContainer, View.VISIBLE, application.getFadeInAnimation());

                // grab the focus and show the keyboard
                characterDisplayView.requestFocus();

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(characterDisplayView, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        Picasso.with(DeviceInteractionActivity.this).load(R.drawable.touchpad_surface).fit().into(touchPadArea, new com.squareup.picasso.Callback()
        {
            @Override
            public void onSuccess()
            {
                touchPadArea.setEnabled(true);
            }

            @Override
            public void onError()
            {
                textView.setText("Error loading touch pad background"); // TODO: strings.xml
                SystemMessagingUtils.showShortToast(DeviceInteractionActivity.this, "Error loading touch pad background");
            }
        });

        touchPadArea.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                return gestureHandler.onTouchEvent(event);
            }
        });
    }

    private void initialiseSpecialKeyboardInteractions()
    {
        // TODO: add special keys here F1-12
        View keyboardContainerView = tabbedViewPager.getTabbedAdapter().getViewByInteractionTab(InteractionTab.KEYBOARD);
    }

    private void initialiseSystemInteractions()
    {
        View systemContainerView = tabbedViewPager.getTabbedAdapter().getViewByInteractionTab(InteractionTab.SYSTEM);

        ImageButton lockMachine = (ImageButton) systemContainerView.findViewById(R.id.imageBtn_lock);
        ImageButton restart = (ImageButton) systemContainerView.findViewById(R.id.imageBtn_restart);
        ImageButton shutdown = (ImageButton) systemContainerView.findViewById(R.id.imageBtn_shutdown);

        lockMachine.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                JsonObject object = messageBuilder.getBaseObject();
                object.addProperty(Constants.KEY_IDENTIFIER, Constants.KEY_VOICE);
                object.addProperty(Constants.KEY_VALUE, "lock_machine");

                connectionService.write(messageBuilder.toJson(object));
            }
        });

        restart.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                systemControlDialogBuilder
                        .setMessage("Are you sure you want to restart the machine?")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                // TODO: write to server
                                SystemMessagingUtils.showShortToast(DeviceInteractionActivity.this, "Implement Restart");
                            }
                        })
                        .create()
                        .show();
            }
        });

        shutdown.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                systemControlDialogBuilder
                        .setMessage("Are you sure you want to shutdown the machine?")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                // TODO: write to server
                                SystemMessagingUtils.showShortToast(DeviceInteractionActivity.this, "Implement Shutdown");
                            }
                        })
                        .create()
                        .show();
            }
        });

        Button filePicker = (Button) systemContainerView.findViewById(R.id.button_showFilePicker);
        filePicker.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                // This always works
                Intent intent = new Intent(DeviceInteractionActivity.this, FilePickerActivity.class);
                // This works if you defined the intent filter
                // Intent i = new Intent(Intent.ACTION_GET_CONTENT);

                // Set these depending on your use case. These are the defaults.
                intent.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, true);
                intent.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                intent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);

                // Configure initial directory by specifying a String.
                // You could specify a String like "/storage/emulated/0/", but that can
                // dangerous. Always use Android's API calls to get paths to the SD-card or
                // internal memory.
                intent.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

                startActivityForResult(intent, REQUEST_FILE_PICKER);
            }
        });

        Button startClipboardActivity = (Button) systemContainerView.findViewById(R.id.button_start_clipboard_manager_activity);
        startClipboardActivity.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ActivityUtils.simpleStartActivityForResult(DeviceInteractionActivity.this, ClipboardManagerActivity.class, REQUEST_CLIPBOARD_MANAGER);
            }
        });

        systemVolumeSeekBar = (SeekBar) systemContainerView.findViewById(R.id.seekBar_systemVolume);
        systemVolumeSeekBar.setProgress(Integer.valueOf(initialSyncData[1]));
        systemVolumeSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                JsonObject object = messageBuilder.getBaseObject();
                object.addProperty(Constants.KEY_IDENTIFIER, Constants.KEY_VOLUME);
                object.addProperty(Constants.KEY_VOLUME_ENABLED, true);
                object.addProperty(Constants.KEY_VALUE, progress);

                connectionService.write(messageBuilder.toJson(object));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
            }
        };
        systemVolumeSeekBar.setOnSeekBarChangeListener(systemVolumeSeekBarChangeListener);

        muteButton = (ToggleButton) systemContainerView.findViewById(R.id.toggleButton_systemVolumeMute);
        muteButton.setChecked(Boolean.valueOf(initialSyncData[0]));
        muteButtonCheckedChangeListener = new ToggleButtonCheckedChangeListener();
        muteButton.setOnCheckedChangeListener(muteButtonCheckedChangeListener);
        
        screenBrightnessSeekBar = (SeekBar) systemContainerView.findViewById(R.id.seekBar_screenBrightness);
        screenBrightnessSeekBar.setProgress(Integer.valueOf(initialSyncData[2]));
        screenBrightnessSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                JsonObject object = messageBuilder.getBaseObject();
                object.addProperty(Constants.KEY_IDENTIFIER, Constants.KEY_SCREEN);
                object.addProperty(Constants.KEY_VALUE, progress);

                connectionService.write(messageBuilder.toJson(object));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
            }
        };
        screenBrightnessSeekBar.setOnSeekBarChangeListener(screenBrightnessSeekBarChangeListener);
        fileSenderManager = new FileSenderManager(this);
    }

    private void initialiseVoiceInteractions()
    {
        final View voiceContainerView = tabbedViewPager.getTabbedAdapter().getViewByInteractionTab(InteractionTab.VOICE);
        txtSpeechInput = (TextView) voiceContainerView.findViewById(R.id.txtSpeechInput);
        btnSpeak = (ImageButton) voiceContainerView.findViewById(R.id.btnSpeak);

        btnSpeak.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!isSpeechRecogniserListening)
                {
                    speechRecogniser.startListening(speechRecogniserIntent);
                }
            }
        });
    }

    private void setMuteButtonChecked(boolean isChecked)
    {
        muteButton.setOnCheckedChangeListener(null);
        muteButton.setChecked(isChecked);
        muteButton.setOnCheckedChangeListener(muteButtonCheckedChangeListener);
    }

    private final BroadcastReceiver batteryStateReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (serverSyncUpInitialised)
            {
                String action = intent.getAction();

                JsonObject json = messageBuilder.getBaseObject();
                json.addProperty(Constants.KEY_IDENTIFIER, Constants.KEY_BATTERY);

                if (action.equals(Intent.ACTION_BATTERY_CHANGED))
                {
                    int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                    boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;

                    if (!isCharging)
                    {
                        json.addProperty(Constants.KEY_BATTERY_CHARGING, false);
                    }
                    else
                    {
                        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                        boolean isUsbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;

                        json.addProperty(Constants.KEY_BATTERY_CHARGING, true);
                        json.addProperty(Constants.KEY_BATTERY_CHARGE_TYPE, (isUsbCharge ? Constants.VALUE_CHARGE_TYPE_USB : Constants.VALUE_CHARGE_TYPE_AC));
                    }
                }
                else if (action.equals(Intent.ACTION_BATTERY_LOW))
                {
                    json.addProperty(Constants.KEY_BATTERY_STATE, Constants.VALUE_CHARGE_LOW);
                }
                else if (action.equals(Intent.ACTION_BATTERY_OKAY))
                {
                    json.addProperty(Constants.KEY_BATTERY_STATE, Constants.VALUE_CHARGE_OK);
                }

                connectionService.write(messageBuilder.toJson(json));
            }
        }
    };

    @Override
    public void onFileQueued(QueuedFile queuedFile)
    {
        JsonObject object = messageBuilder.getBaseObject();
        object.addProperty(Constants.KEY_IDENTIFIER, Constants.KEY_FILE);
        object.addProperty(Constants.KEY_ACTION, Constants.VALUE_BEGIN_FILE_TRANSFER);
        object.addProperty(Constants.KEY_NAME, queuedFile.getFileName());
        object.addProperty(Constants.KEY_TOTAL, queuedFile.getTotalBytes());
        object.addProperty(Constants.KEY_CHECKSUM, queuedFile.getChecksum());

        connectionService.write(messageBuilder.toJson(object));
    }

    @Override
    public void onFileAccepted(QueuedFile queuedFile)
    {
        fileSendNotificationBuilder.setProgress(100, 0, false);

        // Displays the progress bar for the first time.
        notifyManager.notify(DeviceInteractionActivity.ID_FILE_SEND_NOTIFICATION, fileSendNotificationBuilder.build());

        fileSenderManager.prepareNextAvailable();
    }

    @Override
    public void onFileSending(QueuedFile queuedFile, byte[] fileBytes, int remainingBytes, int progress)
    {
        fileSendNotificationBuilder.setProgress(100, progress, false);
        notifyManager.notify(DeviceInteractionActivity.ID_FILE_SEND_NOTIFICATION, fileSendNotificationBuilder.build());

        JsonObject object = messageBuilder.getBaseObject();
        object.addProperty(Constants.KEY_IDENTIFIER, Constants.KEY_FILE);
        object.addProperty(Constants.KEY_ACTION, Constants.VALUE_FILE_SENDING);
        object.addProperty(Constants.KEY_NAME, queuedFile.getFileName());
        object.addProperty(Constants.KEY_CHECKSUM, queuedFile.getChecksum());
        object.addProperty(Constants.KEY_VALUE, Base64.encodeToString(fileBytes, Base64.DEFAULT));
        object.addProperty(Constants.KEY_REMAINING, remainingBytes);

        connectionService.write(messageBuilder.toJson(object));
    }

    @Override
    public void onFileSendFinished(QueuedFile finishedFile)
    {
        if (finishedFile != null)
        {
            fileSendNotificationBuilder
                    .setContentText("File upload complete!")
                    // Removes the progress bar
                    .setProgress(0, 0, false);

            notifyManager.notify(DeviceInteractionActivity.ID_FILE_SEND_NOTIFICATION, fileSendNotificationBuilder.build());
        }
    }

    protected class ToggleButtonCheckedChangeListener implements CompoundButton.OnCheckedChangeListener
    {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            JsonObject json = messageBuilder.getBaseObject();

            json.addProperty(Constants.KEY_IDENTIFIER, Constants.KEY_VOLUME);
            json.addProperty(Constants.KEY_VOLUME_ENABLED, isChecked);

            connectionService.write(messageBuilder.toJson(json)); // true for mute
        }
    }

    protected class SpeechRecognitionListener implements RecognitionListener
    {
        @Override
        public void onBeginningOfSpeech()
        {
        }

        @Override
        public void onBufferReceived(byte[] buffer)
        {
        }

        @Override
        public void onEndOfSpeech()
        {
        }

        @Override
        public void onError(int error)
        {
            speechRecogniser.startListening(speechRecogniserIntent);
        }

        @Override
        public void onEvent(int eventType, Bundle params)
        {
        }

        @Override
        public void onPartialResults(Bundle partialResults)
        {
        }

        @Override
        public void onReadyForSpeech(Bundle params)
        {
        }

        @Override
        public void onResults(Bundle results)
        {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            if (matches != null && matches.size() > 0)
            {
                String recognisedText = matches.get(0);

                txtSpeechInput.setText(recognisedText);

                JsonObject object = messageBuilder.getBaseObject();
                object.addProperty(Constants.KEY_IDENTIFIER, Constants.KEY_VOICE);
                object.addProperty(Constants.KEY_VALUE, recognisedText);

                connectionService.write(messageBuilder.toJson(object));
            }
        }

        @Override
        public void onRmsChanged(float rmsdB)
        {
        }
    }
}