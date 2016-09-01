package com.eyssyapps.bluetoothcommandsender.custom;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TabLayout;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.eyssyapps.bluetoothcommandsender.adapters.TabbedViewPagerAdapter;
import com.eyssyapps.bluetoothcommandsender.state.InteractionTab;
import com.eyssyapps.bluetoothcommandsender.state.models.TabPageMetadata;

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

        initialise(changeListener, inflatablePageMetadata);
    }

    private void initialise(ViewPager.OnPageChangeListener changeListener, List<TabPageMetadata> inflatablePageMetadata)
    {
        adapter = new TabbedViewPagerAdapter(context, inflatablePageMetadata);

        viewPager = (ViewPager) parentView.findViewById(viewPagerTabLayoutResIds.first);
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
