package com.eyssyapps.utilities_suite.services;

import android.support.annotation.NonNull;
import android.view.Menu;

import java.util.HashMap;
import java.util.List;

/**
 * Created by eyssy on 01/09/2016.
 */
public class MenuCachingService
{
    // TODO: evaluate if this service is any good at all
    private static MenuCachingService SERVICE;

    private HashMap<String, Menu> state;

    public static MenuCachingService getDefaultService()
    {
        if (SERVICE == null)
        {
            SERVICE = new MenuCachingService();
        }

        return SERVICE;
    }

    private MenuCachingService()
    {
        state = new HashMap<>();
    }

    public synchronized boolean add(String key, @NonNull Menu menu)
    {
        if (state.containsKey(key))
        {
            return false;
        }

        state.put(key, menu);

        return true;
    }

    public synchronized boolean remove(String key, @NonNull Menu menu)
    {
        if (state.containsKey(key))
        {
            state.remove(menu);

            return true;
        }

        return false;
    }

    public synchronized boolean removeByKey(String key)
    {
        Menu toRemove = state.get(key);

        if (toRemove == null)
        {
            return false;
        }

        state.remove(toRemove);

        return true;
    }

    public synchronized void clear()
    {
        state.clear();
    }

    public synchronized void removeAllByKeys(List<String> keys)
    {
        for (String key : keys)
        {
            removeByKey(key);
        }
    }
}
