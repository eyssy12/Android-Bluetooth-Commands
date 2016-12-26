package com.zagorapps.utilities_suite.custom;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TabLayout;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
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

    private ViewPager viewPager;
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
        adapter = new TabbedViewPagerAdapter(context, inflatablePageMetadata);

        viewPager = (ViewPager) parentView.findViewById(viewPagerTabLayoutResIds.first);
        viewPager.setOffscreenPageLimit(inflatablePageMetadata.size());
        viewPager.setAdapter(adapter);

        tabLayout = (TabLayout) parentView.findViewById(viewPagerTabLayoutResIds.second);
        tabLayout.setupWithViewPager(viewPager);

        for (int i = 0; i < inflatablePageMetadata.size(); i++)
        {
            TabPageMetadata metadata = inflatablePageMetadata.get(i);

            tabLayout.getTabAt(i).setIcon(metadata.getDrawableResId());
        }

        setCurrentTab(defaultTab);

        viewPager.addOnPageChangeListener(changeListener);
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                return true;
            }
        });
    }

    public void disablePageListener()
    {
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                return true;
            }
        });
    }

    public void enablePageListener()
    {
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                return false;
            }
        });
    }

    public void setTabIcon(InteractionTab which, Drawable drawable)
    {
        tabLayout.getTabAt(which.getOrder()).setIcon(drawable);
    }

    public void setTabIcon(InteractionTab which, int drawableId)
    {
        tabLayout.getTabAt(which.getOrder()).setIcon(drawableId);
    }
    
    public void setCurrentTab(InteractionTab tab)
    {
        viewPager.setCurrentItem(tab.getOrder());
    }

    public ViewPager getViewPager()
    {
        return viewPager;
    }

    public InteractionTab getDefaultTab()
    {
        return defaultTab;
    }

    public TabbedViewPagerAdapter getTabbedAdapter()
    {
        return adapter;
    }
}
