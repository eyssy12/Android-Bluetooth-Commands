package com.zagorapps.utilities_suite.custom;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TabLayout;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.zagorapps.utilities_suite.adapters.TabbedViewPagerAdapter;
import com.zagorapps.utilities_suite.state.InteractionTab;
import com.zagorapps.utilities_suite.state.models.TabPageMetadata;

import java.util.List;

/**
 * Created by eyssy on 26/08/2016.
 */
public class TabbedViewPager
{
    private final Context context;
    private final View parentView;

    private final Pair<Integer, Integer> viewPagerTabLayoutResIds;

    private CustomViewPager viewPager;
    private TabLayout tabLayout;
    private TabbedViewPagerAdapter adapter;
    private InteractionTab defaultTab;
    private ViewPager.OnPageChangeListener changeListener;

    public TabbedViewPager(
        Context context,
        View parentView,
        ViewPager.OnPageChangeListener changeListener,
        Pair<Integer, Integer> viewPagerTabLayoutResIds,
        List<TabPageMetadata> inflatablePageMetadata)
    {
        this.context = context;
        this.parentView = parentView;
        this.viewPagerTabLayoutResIds = viewPagerTabLayoutResIds;
        this.defaultTab = InteractionTab.getDefaultTab();
        this.changeListener = changeListener;

        initialise(inflatablePageMetadata);
    }

    private void initialise(List<TabPageMetadata> inflatablePageMetadata)
    {
        this.adapter = new TabbedViewPagerAdapter(this.context, inflatablePageMetadata);

        this.viewPager = (CustomViewPager) this.parentView.findViewById(this.viewPagerTabLayoutResIds.first);
        this.viewPager.setOffscreenPageLimit(inflatablePageMetadata.size());
        this.viewPager.setAdapter(this.adapter);

        this.tabLayout = (TabLayout) this.parentView.findViewById(this.viewPagerTabLayoutResIds.second);
        this.tabLayout.setupWithViewPager(this.viewPager);

        for (int i = 0; i < inflatablePageMetadata.size(); i++)
        {
            TabPageMetadata metadata = inflatablePageMetadata.get(i);

            this.tabLayout.getTabAt(i).setIcon(metadata.getDrawableResId());
        }

        this.setCurrentTab(this.defaultTab);

        this.viewPager.addOnPageChangeListener(this.changeListener);
        this.viewPager.setSwipeEnabled(false);
    }

    public void disablePageListener()
    {
        this.viewPager.setSwipeEnabled(false);
    }

    public void enablePageListener()
    {
        this.viewPager.setSwipeEnabled(true);
    }

    public void setTabIcon(InteractionTab which, Drawable drawable)
    {
        this.tabLayout.getTabAt(which.getOrder()).setIcon(drawable);
    }

    public void setTabIcon(InteractionTab which, int drawableId)
    {
        this.tabLayout.getTabAt(which.getOrder()).setIcon(drawableId);
    }
    
    public void setCurrentTab(InteractionTab tab)
    {
        this.viewPager.setCurrentItem(tab.getOrder());
    }

    public ViewPager getViewPager()
    {
        return this.viewPager;
    }

    public InteractionTab getDefaultTab()
    {
        return this.defaultTab;
    }

    public TabbedViewPagerAdapter getTabbedAdapter()
    {
        return this.adapter;
    }
}
