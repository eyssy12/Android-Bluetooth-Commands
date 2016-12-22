package com.zagorapps.utilities_suite.state;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.zagorapps.utilities_suite.R;

/**
 * Created by eyssy on 24/08/2016.
 */
public class ComplexPreferences
{
    private static final String DEFAULT_NAMED_PREFERENCES = "complex_preferences";

    private static ComplexPreferences COMPLEX_PREFERENCES;
    private static Gson GSON = new Gson();
    
    private Context context;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private ComplexPreferences(Context context, String namePreferences, int mode)
    {
        this.context = context;

        if (TextUtils.isEmpty(namePreferences))
        {
            namePreferences = DEFAULT_NAMED_PREFERENCES;
        }

        preferences = context.getSharedPreferences(namePreferences, mode);
        editor = preferences.edit();
    }

    public static ComplexPreferences getComplexPreferences(Context context, String namePreferences, int mode)
    {
        if (COMPLEX_PREFERENCES == null)
        {
            COMPLEX_PREFERENCES = new ComplexPreferences(context, namePreferences, mode);
        }

        return COMPLEX_PREFERENCES;
    }

    public void putObject(String key, Object object)
    {
        if (object == null)
        {
            throw new IllegalArgumentException(context.getString(R.string.illegal_argument_exception_no_object_reference_provided));
        }

        if (TextUtils.isEmpty(key))
        {
            throw new IllegalArgumentException(context.getString(R.string.illegal_argument_exception_no_key_provided));
        }

        editor.putString(key, GSON.toJson(object));
    }

    public void clear()
    {
        editor.clear();
        editor.commit();
    }

    public void commit()
    {
        editor.commit();
    }

    public <T> T getObject(String key, Class<T> a)
    {
        String gson = preferences.getString(key, null);

        if (gson == null)
        {
            return null;
        }
        else
        {
            try
            {
                return GSON.fromJson(gson, a);
            }
            catch (Exception e)
            {
                String format = context.getString(R.string.illegal_argument_exception_key_instance_of_another_class);

                throw new IllegalArgumentException(String.format(format, key));
            }
        }
    }
}