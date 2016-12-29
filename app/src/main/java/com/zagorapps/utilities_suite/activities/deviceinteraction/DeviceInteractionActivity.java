package com.zagorapps.utilities_suite.activities.deviceinteraction;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.design.widget.AppBarLayout;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
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
import android.view.WindowManager;
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
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.zagorapps.utilities_suite.R;
import com.zagorapps.utilities_suite.custom.TabbedViewPager;
import com.zagorapps.utilities_suite.handlers.BluetoothGestureEventHandler;
import com.zagorapps.utilities_suite.handlers.BluetoothMessageHandler;
import com.zagorapps.utilities_suite.interfaces.OnBluetoothMessageListener;
import com.zagorapps.utilities_suite.protocol.Commands;
import com.zagorapps.utilities_suite.protocol.ServerCommands;
import com.zagorapps.utilities_suite.state.InteractionTab;
import com.zagorapps.utilities_suite.state.models.BluetoothDeviceLite;
import com.zagorapps.utilities_suite.state.models.TabPageMetadata;
import com.zagorapps.utilities_suite.threading.BluetoothConnectionThread;
import com.zagorapps.utilities_suite.utils.threading.RunnableUtils;
import com.zagorapps.utilities_suite.utils.view.ActivityUtils;
import com.zagorapps.utilities_suite.utils.view.SystemMessagingUtils;
import com.zagorapps.utilities_suite.utils.view.ViewUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeviceInteractionActivity extends AppCompatActivity implements
        OnBluetoothMessageListener
{
    public static final String DEVICE_KEY = "device",
            MOUSE_SENSITIVITY_KEY = "mouse_sensitivity";

    public static final int REQUEST_INTERACTION_SETTINGS = 100;

    private static final float DEFAULT_MOUSE_SENSITIVITY = (float) 1.1;

    private static UUID SERVER_ENDPOINT = UUID.fromString("1f1aa577-32d6-4c59-b9a2-f262994783e9");
    private static float MOUSE_SENSITIVITY = DEFAULT_MOUSE_SENSITIVITY;

    private int previousTextCount = 0;
    private int keyboardInteractionViewId;
    private boolean keyboardInteractionInitiated = false;
    private boolean doubleBackToExitPressedOnce = false;

    private View parentView;
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;
    private TabbedViewPager tabbedViewPager;
    private InteractionTab currentTab, previousTab;

    private Animation fadeInAnimation, fadeOutAnimation;
    private ProgressDialog progressDialog;
    private TextView textView;
    private ImageView touchPadArea;

    private TextView txtSpeechInput;
    private ImageButton btnSpeak;

    private SpeechRecognizer speechRecogniser;
    private Intent speechRecogniserIntent;
    private boolean isSpeechRecogniserListening = false;

    private AlertDialog machineLockedDialog;

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

        try
        {
            SERVER_ENDPOINT = UUID.nameUUIDFromBytes("12345".getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        targetDevice = getIntent().getExtras().getParcelable(DEVICE_KEY);

        prepareStatics();
        prepareMachineAlertDialog();
        prepareProgressDialog();
        prepareTabbedView();
        prepareBluetoothHandlers();
        prepareVoiceRecogniser();

        gestureHandler.beginHandler();
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
                connectionThread.write(Commands.END_SESSION.toString());
            }
            else
            {
                this.doubleBackToExitPressedOnce = true;

                SystemMessagingUtils.showToast(this,
                        "Please click BACK again to exit",
                        Toast.LENGTH_SHORT);

                RunnableUtils.ExecuteWithDelay(new Runnable()
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
    public void onMessageRead(String message)
    {
        if (message.equals(ServerCommands.CLOSE.toString()))
        {
            connectionThread.close();
        }
        else if (message.equals("Permitted Process Running"))
        {
            SystemMessagingUtils.showShortToast(this,
                    "Unable to perform system command since more than one permitted process is running.");
        }
        else if (message.equals("machine_locked"))
        {
            machineLockedDialog.show();
        }
        else if (message.equals("machine_unlocked"))
        {
            machineLockedDialog.dismiss();
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
                MOUSE_SENSITIVITY = data.getFloatExtra(MOUSE_SENSITIVITY_KEY,
                        DEFAULT_MOUSE_SENSITIVITY);

                gestureHandler.setMouseSensitivity(MOUSE_SENSITIVITY);
            }
        }
    }

    private void prepareVoiceRecogniser()
    {
        speechRecogniserIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecogniserIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecogniserIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());

        speechRecogniser = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecogniser.setRecognitionListener(new SpeechRecognitionListener());
    }

    private void prepareStatics()
    {
        fadeInAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeInAnimation.setInterpolator(new AccelerateInterpolator());
        fadeInAnimation.setDuration(200);

        fadeOutAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        fadeOutAnimation.setInterpolator(new AccelerateInterpolator());
        fadeOutAnimation.setDuration(200);
    }

    private void prepareMachineAlertDialog()
    {
        machineLockedDialog = new AlertDialog.Builder(this)
                .setTitle("Machine Locked")
                .setIcon(R.drawable.ic_phonelink_lock_black_48dp)
                .setCancelable(false)
                .create();

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

    private void prepareTabbedView()
    {
        Pair<Integer, Integer> viewPagerTabLayoutResIds = new Pair<>(
                R.id.device_interaction_view_pager,
                R.id.device_interaction_tabs);

        List<TabPageMetadata> inflatablePageMetadata = new ArrayList<>();
        inflatablePageMetadata.add(new TabPageMetadata(InteractionTab.MOUSE,
                R.layout.content_device_interaction_mouse,
                R.drawable.ic_mouse_white_48dp));
        // TODO: will most likely re-add this back to allow functionality for some F1-12 keys and more
        // inflatablePageMetadata.add(new TabPageMetadata(InteractionTab.KEYBOARD, R.layout.content_device_interaction_keyboard, R.drawable.ic_keyboard_white_48dp));
        inflatablePageMetadata.add(new TabPageMetadata(InteractionTab.SYSTEM,
                R.layout.content_device_interaction_system,
                R.drawable.ic_assignment_white_48dp));
        inflatablePageMetadata.add(new TabPageMetadata(InteractionTab.VOICE,
                R.layout.content_device_interaction_voice,
                R.drawable.ic_record_voice_over_white_48dp));

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

        tabbedViewPager = new TabbedViewPager(this,
                parentView,
                changeListener,
                viewPagerTabLayoutResIds,
                inflatablePageMetadata);

        currentTab = tabbedViewPager.getDefaultTab();
        previousTab = InteractionTab.NOT_SET;
    }

    private void prepareBluetoothHandlers()
    {
        messageHandler = new BluetoothMessageHandler(this);
        // TODO: pass down the password that will create the server endpoint out of it as a UUID
        connectionThread = new BluetoothConnectionThread(SERVER_ENDPOINT,
                targetDevice,
                messageHandler);
        gestureHandler = new BluetoothGestureEventHandler(this,
                connectionThread,
                MOUSE_SENSITIVITY);
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
        final RelativeLayout mouseContainerView = (RelativeLayout) tabbedViewPager.getTabbedAdapter()
                                                                                  .getViewByInteractionTab(
                                                                                          InteractionTab.MOUSE);
        final View mouseInteractionContainer = mouseContainerView.findViewById(R.id.mouse_interaction_container);
        final View keyboardInteractionInitiatorContainer = mouseContainerView.findViewById(R.id.keyboard_interaction_initiator_container);
        final LinearLayout keyboardInteractionContainer = (LinearLayout) mouseContainerView.findViewById(
                R.id.keyboard_interaction_container);

        this.textView = (TextView) mouseInteractionContainer.findViewById(R.id.received_data_text);
        this.touchPadArea = (ImageView) mouseInteractionContainer.findViewById(R.id.touchpad_area);
        this.touchPadArea.setEnabled(false);

        final ImageButton keyboardBtn = (ImageButton) keyboardInteractionInitiatorContainer.findViewById(
                R.id.keyboard_image_btn);

        final List<View> mouseTouchPadInteractiveViews = new ArrayList<>();
        mouseTouchPadInteractiveViews.add(this.appBarLayout);
        mouseTouchPadInteractiveViews.add(mouseInteractionContainer);
        mouseTouchPadInteractiveViews.add(keyboardInteractionInitiatorContainer);

        keyboardBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // hide all the current views
                ViewUtils.setViewAndChildrenVisibility(mouseTouchPadInteractiveViews,
                        View.GONE,
                        fadeOutAnimation);

                EditText keyboardTextView;
                if (keyboardInteractionInitiated)
                {
                    keyboardTextView = (EditText) keyboardInteractionContainer.findViewById(
                            keyboardInteractionViewId);
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

                                // the Soft keyboard has a delay when it is disposed hence it doesnt look as nice when the other views get rendered before it
                                RunnableUtils.ExecuteWithDelay(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        ViewUtils.setViewAndChildrenVisibility(
                                            mouseTouchPadInteractiveViews,
                                            View.VISIBLE,
                                            fadeInAnimation);

                                        ViewUtils.setViewAndChildrenVisibility(
                                            keyboardInteractionContainer,
                                            View.GONE,
                                            fadeOutAnimation);
                                    }
                                }, 250);
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
                    keyboardTextView.setOnKeyListener(new View.OnKeyListener()
                    {
                        @Override
                        public boolean onKey(View v, int keyCode, KeyEvent event)
                        {
                            // TODO: this doesn't work as expected ->
                            // the amount of times the user presses a character keycode in the soft keyboard on the edittext,
                            // the amount times + 1 it takes before the keyListener starts registering the delete key event.

                            // onKey gets called twice on a button press -> 1 for ACTION_DOWN, 2 for ACTION_UP
                            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_DEL)
                            {
                                connectionThread.write(ServerCommands.BACKSPACE.toString());
                            }

                            return true;
                        }
                    });
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
                            // TODO: revisit this function

                            int count = s.length();

                            if (count > previousTextCount)
                            {
                                connectionThread.write(s.subSequence(count - 1, count).toString());
                            }

                            previousTextCount = count;
                        }
                    });

                    keyboardInteractionContainer.addView(keyboardTextView);

                    keyboardInteractionInitiated = true;
                }

                ViewUtils.setViewAndChildrenVisibility(keyboardInteractionContainer,
                        View.VISIBLE,
                        fadeInAnimation);

                // grab the focus and show the keyboard
                keyboardTextView.requestFocus();

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(keyboardTextView, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        Picasso.with(DeviceInteractionActivity.this)
               .load(R.drawable.touchpad_surface)
               .fit()
               .into(touchPadArea, new com.squareup.picasso.Callback()
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
                       SystemMessagingUtils.showShortToast(DeviceInteractionActivity.this,
                               "Error loading touch pad background");
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
        View keyboardContainerView = tabbedViewPager.getTabbedAdapter()
                                                    .getViewByInteractionTab(InteractionTab.KEYBOARD);
    }

    private void initialiseSystemInteractions()
    {
        View systemContainerView = tabbedViewPager.getTabbedAdapter()
                                                  .getViewByInteractionTab(InteractionTab.SYSTEM);
    }

    private void initialiseVoiceInteractions()
    {
        final View voiceContainerView = tabbedViewPager.getTabbedAdapter()
                                                       .getViewByInteractionTab(InteractionTab.VOICE);
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

    protected class SpeechRecognitionListener implements RecognitionListener
    {
        @Override
        public void onBeginningOfSpeech()
        {
            //Log.d(TAG, "onBeginingOfSpeech");
        }

        @Override
        public void onBufferReceived(byte[] buffer)
        {

        }

        @Override
        public void onEndOfSpeech()
        {
            //Log.d(TAG, "onEndOfSpeech");
        }

        @Override
        public void onError(int error)
        {
            speechRecogniser.startListening(speechRecogniserIntent);

            //Log.d(TAG, "error = " + error);
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
            //Log.d(TAG, "onResults"); //$NON-NLS-1$
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            // matches are the return values of speech recognition engine
            // Use these values for whatever you wish to do

            String recognisedText = matches.get(0);

            txtSpeechInput.setText(recognisedText);
            connectionThread.write(recognisedText);
        }

        @Override
        public void onRmsChanged(float rmsdB)
        {
        }
    }
}