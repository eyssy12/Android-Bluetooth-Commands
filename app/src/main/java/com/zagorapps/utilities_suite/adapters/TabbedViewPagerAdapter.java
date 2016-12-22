package com.zagorapps.utilities_suite.adapters;

import android.content.Context;
import android.support.v4.util.Pair;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zagorapps.utilities_suite.state.InteractionTab;
import com.zagorapps.utilities_suite.state.models.TabPageMetadata;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eyssy on 26/08/2016.
 */
public class TabbedViewPagerAdapter extends PagerAdapter
{
    private Context context;
    private LayoutInflater inflater;
    private List<TabPageMetadata> inflatablePageMetadata;
    private List<Pair<InteractionTab, View>> views;

    public TabbedViewPagerAdapter(Context context, List<TabPageMetadata> inflatablePageMetadata)
    {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.inflatablePageMetadata = inflatablePageMetadata;
        this.views = new ArrayList<>(inflatablePageMetadata.size());
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        return inflatablePageMetadata.get(position).getTab().toString();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position)
    {
        View page = inflater.inflate(inflatablePageMetadata.get(position).getLayoutResId(), container, false);

        views.add(new Pair<>(inflatablePageMetadata.get(position).getTab(), page));

        container.addView(page);

        return page;
    }

    @Override
    public int getCount()
    {
        return inflatablePageMetadata.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object)
    {
        return view.equals(object);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object)
    {
        // no need to destroy the item since there should only be a maximum of 3-4 tabs
       // container.removeView((View) object);
    }

    public View getViewByInteractionTab(InteractionTab requested)
    {
        boolean found = false;
        int i = 0;

        View result = null;
        while (i < views.size() && !found)
        {
            Pair<InteractionTab, View> pair = views.get(i);

            if (pair.first == requested)
            {
                result = pair.second;
                found = true;
            }
            else
            {

                i++;
            }
        }

        return result;
    }
}