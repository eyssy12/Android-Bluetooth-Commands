package com.eyssyapps.bluetoothcommandsender.utils.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * Created by eyssy on 04/12/2015.
 */
public class ViewUtils
{
    public static boolean isValid(EditText text)
    {
        return isValid(text, "Something is not right");
    }

    public static boolean isValid(EditText text, String errorMessage)
    {
        return isValid(text, errorMessage, null);
    }

    public static boolean isValid(EditText text, String errorMessage, Pattern pattern)
    {
        String value = text.getText().toString();

        if (value.isEmpty())
        {
            text.setError(errorMessage);
            return false;
        }
        else if (!(pattern == null || pattern.matcher(value).matches()))
        {
            text.setError(errorMessage);
            return false;
        }

        text.setError(null);
        return true;
    }

    public static void startImagePickerActivity(Activity context, int requestCode)
    {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        context.startActivityForResult(chooserIntent, requestCode);
    }

    public static Drawable drawableFromUrl(String url) throws IOException
    {
        Bitmap x;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.connect();
        InputStream input = connection.getInputStream();

        x = BitmapFactory.decodeStream(input);
        return new BitmapDrawable(x);
    }

    public static void setViewAndChildrenVisibility(View view, int visibility)
    {
        view.setVisibility(visibility);

        if (view instanceof ViewGroup)
        {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++)
            {
                View child = viewGroup.getChildAt(i);
                setViewAndChildrenVisibility(child, visibility);
            }
        }
    }

    public static void simulateTouchEventForView(View view)
    {
        // Obtain MotionEvent object
        long downTime = SystemClock.uptimeMillis(), eventTime = SystemClock.uptimeMillis() + 100;
        float x = 0.0f, y = 0.0f;

        // List of meta states found here: developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
        int metaState = 0;
        MotionEvent motionEvent = MotionEvent.obtain(
            downTime,
            eventTime,
            MotionEvent.ACTION_MOVE,
            x,
            y,
            metaState);

        // Dispatch touch event to view
        view.dispatchTouchEvent(motionEvent);
    }
}
