package com.eyssyapps.bluetoothcommandsender.activities.deviceinteraction;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.eyssyapps.bluetoothcommandsender.R;

public class InteractionSettingsActivity extends Activity implements View.OnClickListener
{
    private SeekBar mouseSensitivitySeekBar;
    private TextView mouseSensitivityValueTextView;
    private ImageButton saveSettingsButton;

    private float mouseSensitivity;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interaction_settings);

        Bundle extras = getIntent().getExtras();
        mouseSensitivity = extras.getFloat(DeviceInteractionActivity.MOUSE_SENSITIVITY_KEY, 3);

        mouseSensitivityValueTextView = (TextView) findViewById(R.id.mouse_sensitivity_value);
        saveSettingsButton = (ImageButton) findViewById(R.id.save_settings_button);

        saveSettingsButton.setOnClickListener(this);

        mouseSensitivitySeekBar = (SeekBar) findViewById(R.id.mouse_sensitivity_seekbar);
        mouseSensitivitySeekBar.setMax(10);
        mouseSensitivitySeekBar.incrementProgressBy(1);
        mouseSensitivitySeekBar.setProgress((int)mouseSensitivity * 10);
        mouseSensitivityValueTextView.setText(String.valueOf(mouseSensitivity));

        mouseSensitivitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                float newProgress = (float) progress / 10;
                mouseSensitivity = newProgress;

                mouseSensitivityValueTextView.setText(String.valueOf(newProgress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
            }
        });
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.save_settings_button:
                Intent intent = new Intent();
                intent.putExtra(DeviceInteractionActivity.MOUSE_SENSITIVITY_KEY, mouseSensitivity);

                setResult(RESULT_OK, intent);

                finish();
                break;
        }
    }
}
