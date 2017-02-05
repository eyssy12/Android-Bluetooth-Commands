package com.zagorapps.utilities_suite.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by eyssy on 01/05/2016.
 */
public abstract class RecyclerViewAdapterBase<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH>
{
    protected Context context;
    protected LayoutInflater inflater;
    protected LinkedList<T> items;
    protected final View parentView;

    protected int selectedPosition = -1;

    private final Object mutex;

    protected RecyclerViewAdapterBase(Context context, View parentView)
    {
        this.context = context;
        this.parentView = parentView;

        this.inflater = LayoutInflater.from(context);
        this.items = new LinkedList<>();
        this.mutex = new Object();
    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }

    public View getParentView()
    {
        return this.parentView;
    }

    public boolean addAtPosition(T item, int position)
    {
        if (items.contains(item) || position > items.size())
        {
            return false;
        }

        items.add(position, item);
        refresh();

        return true;
    }

    public boolean addLast(T item)
    {
        if (items.contains(item))
        {
            return false;
        }

        items.addLast(item);
        refresh();

        return true;
    }

    public boolean addFirst(T item)
    {
        if (items.contains(item))
        {
            return false;
        }

        items.addFirst(item);
        refresh();

        return true;
    }

    public boolean remove(T item)
    {
        if (items.contains(item))
        {
            items.remove(item);
            refresh();
            return true;
        }

        return false;
    }

    public boolean remove(int position)
    {
        if (position < 0 || position > items.size() - 1)
        {
            return false;
        }

        items.remove(position);

        return true;
    }

    public void addNewCollection(List<T> collection, boolean immediateRefresh)
    {
        items.clear();
        addCollection(collection, immediateRefresh);
    }

    public void addCollection(List<T> collection, boolean immediateRefresh)
    {
        synchronized (mutex)
        {
            if (!collection.isEmpty())
            {
                for (T item : collection)
                {
                    items.addFirst(item);
                }
            }
        }

        if (immediateRefresh)
        {
            refresh();
        }
    }

    public void replaceCollection(List<T> newCollection, boolean immediateRefresh)
    {
        synchronized (mutex)
        {
            if (!newCollection.isEmpty())
            {
                items = new LinkedList<>(newCollection);
            }
        }

        if (immediateRefresh)
        {
            refresh();
        }
    }

    public void clear()
    {
        items.clear();
        refresh();
    }

    public void refresh()
    {
        notifyDataSetChanged();
    }

    public List<T> getItems()
    {
        return (List<T>) this.items.clone();
    }
}