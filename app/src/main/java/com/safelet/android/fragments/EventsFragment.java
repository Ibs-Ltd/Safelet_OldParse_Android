package com.safelet.android.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.parse.ParseObject;
import com.safelet.android.R;
import com.safelet.android.adapters.EventsPagerAdapter;
import com.safelet.android.callback.NavigationDrawerCallbacks;
import com.safelet.android.global.PopDialog;
import com.safelet.android.interactors.EventsManager;
import com.safelet.android.interactors.UserManager;
import com.safelet.android.interactors.callbacks.OnResponseListCallback;
import com.safelet.android.models.enums.NavigationMenu;
import com.safelet.android.models.event.NewNotificationEvent;
import com.safelet.android.utils.Error;
import com.safelet.android.utils.LogoutHelper;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

public class EventsFragment extends BaseFragment implements View.OnClickListener {

    private NavigationDrawerCallbacks parentDrawerCallback;

    private EventsManager eventsManager;
    private UserManager userManager;

    private TextView historyEventsStatusTextView;

    private EventsPagerAdapter eventsPagerAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.parentDrawerCallback = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            // should not happen
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventsManager = EventsManager.instance();
        userManager = UserManager.instance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_events, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.changeHistoryStatus).setOnClickListener(this);
        historyEventsStatusTextView = (TextView) view.findViewById(R.id.historyEventsStatusTextView);
        historyEventsStatusTextView.setOnClickListener(this);
        setHistoryStatus();
        eventsPagerAdapter = new EventsPagerAdapter(getChildFragmentManager());
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.view_pager);
        viewPager.setAdapter(eventsPagerAdapter);
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public int getTitleResId() {
        return R.string.events_title;
    }

    @Override
    protected void onViewNotificationDialogClicked(Context context, NewNotificationEvent event) {
        handleNewNotificationEvent(event);
    }

    @Override
    protected void onNavigationDrawerItemSelected(NavigationMenu navigationMenu) {
        if (!navigationMenu.equals(NavigationMenu.EVENTS)) {
            parentDrawerCallback.onNavigationDrawerItemSelected(navigationMenu);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.historyEventsStatusTextView) {
            showLoading();
            eventsManager.toggleHistoricEvents();
            eventsManager.getEventsForUser(userManager.getUserId(), new GetEventsListener(this));
        }
    }

    @Override
    public void onAlarmClicked() {
        parentDrawerCallback.onNavigationDrawerItemSelected(NavigationMenu.ALARM);
    }

    private void setHistoryStatus() {
        historyEventsStatusTextView.setText(String.format(getString(R.string.history_events),
                eventsManager.includeHistoricEvents() ?
                        getString(R.string.on_text) : getString(R.string.off_text)));
    }

    private static class GetEventsListener implements OnResponseListCallback {

        private WeakReference<EventsFragment> weakReference;

        GetEventsListener(EventsFragment fragment) {
            this.weakReference = new WeakReference<>(fragment);
        }

        @Override
        public void onSuccess(Map<String, List<ParseObject>> objects) {
            EventsFragment fragment = weakReference.get();
            if (fragment != null && fragment.isAdded()) {
                fragment.hideLoading();
                fragment.setHistoryStatus();
                fragment.eventsPagerAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onFailed(Error error) {
            EventsFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            if (!LogoutHelper.handleExpiredSession(fragment.getActivity(), error)) {
                PopDialog.showDialog(fragment.getActivity(),
                        fragment.getString(R.string.events_screen_dialog_error_unabletoretrieve_message));
            }
        }
    }

}
