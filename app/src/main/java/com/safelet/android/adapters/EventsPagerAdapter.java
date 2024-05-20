package com.safelet.android.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.safelet.android.fragments.EventsListFragment;
import com.safelet.android.interactors.EventsManager;


public class EventsPagerAdapter extends FragmentPagerAdapter {

    public EventsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return EventsListFragment.newInstance(EventsManager.EventType.values()[position]);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return EventsManager.EventType.values()[position].toString();
    }

    @Override
    public int getCount() {
        return EventsManager.EventType.values().length;
    }
}
