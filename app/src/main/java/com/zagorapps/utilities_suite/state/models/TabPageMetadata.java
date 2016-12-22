package com.zagorapps.utilities_suite.state.models;

import com.zagorapps.utilities_suite.state.InteractionTab;

/**
 * Created by eyssy on 26/08/2016.
 */
public class TabPageMetadata
{
    private InteractionTab tab;
    private int layoutResId;
    private int drawableResId;

    public TabPageMetadata(InteractionTab tab, int layoutResId, int drawableResId)
    {
        this.tab = tab;
        this.layoutResId = layoutResId;
        this.drawableResId = drawableResId;
    }

    public InteractionTab getTab()
    {
        return tab;
    }

    public int getLayoutResId()
    {
        return layoutResId;
    }

    public int getDrawableResId()
    {
        return drawableResId;
    }
}
