package com.zagorapps.utilities_suite.state.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eyssy on 21/01/2017.
 */

public abstract class CustomAbstractListBase<E> extends ArrayList<E>
{
    public CustomAbstractListBase(List<E> item)
    {
        if (item != null)
        {
            this.addAll(item);
        }
    }

    public boolean addItem(E item)
    {
        if (item == null || this.contains(item))
        {
            return false;
        }

        return this.add(item);
    }

    public boolean removeItem(E item)
    {
        if (item == null || !this.contains(item))
        {
            return false;
        }

        this.remove(item);

        return true;
    }

    public List<E> cloneItems()
    {
        return (List<E>)this.clone();
    }
}
