package com.safelet.android.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.parse.ParseObject;
import com.safelet.android.R;
import com.safelet.android.activities.AlarmActivity;
import com.safelet.android.activities.LastCheckinActivity;
import com.safelet.android.adapters.EventsListAdapter;
import com.safelet.android.global.PopDialog;
import com.safelet.android.global.Utils;
import com.safelet.android.interactors.AlarmManager;
import com.safelet.android.interactors.EventBusManager;
import com.safelet.android.interactors.EventsManager;
import com.safelet.android.interactors.UserManager;
import com.safelet.android.interactors.callbacks.EventsListAdapterCallback;
import com.safelet.android.interactors.callbacks.OnAlarmSoundsCallback;
import com.safelet.android.interactors.callbacks.OnResponseCallback;
import com.safelet.android.interactors.callbacks.OnResponseListCallback;
import com.safelet.android.interactors.utils.ChunkSoundsPlayer;
import com.safelet.android.models.Alarm;
import com.safelet.android.models.AlarmRecordingChunk;
import com.safelet.android.models.CheckIn;
import com.safelet.android.models.GuardianInvitation;
import com.safelet.android.models.enums.UserRelationStatus;
import com.safelet.android.models.event.NewEventsReceivedEvent;
import com.safelet.android.models.event.NoInternetConnectionEvent;
import com.safelet.android.utils.Error;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventsListFragment extends Fragment implements EventsListAdapterCallback, ChunkSoundsPlayer.OnChunkPlayListener {
    private static final String EVENTS_TYPE_KEY = "elf.eventsType.key";
    private static final int TIME_CACHE_MS = 10 * 60 * 1000; // 10 minutes
    public static long sLastUpdateTime;

    private final UserManager userManager = UserManager.instance();
    private final EventsManager eventsManager = EventsManager.instance();

    private ChunkSoundsPlayer chunkSoundsPlayer;

    private ProgressBar loadingProgress;
    private ListView notificationsListView;
    private TextView noNotificationsTextView;

    private List<ParseObject> notificationsList = new ArrayList<>();

    protected EventsListAdapter eventsListAdapter;

    private EventsManager.EventType eventsType = EventsManager.EventType.ALARM;

    public static EventsListFragment newInstance(EventsManager.EventType eventType) {
        EventsListFragment fragment = new EventsListFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EVENTS_TYPE_KEY, eventType.getId());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chunkSoundsPlayer = new ChunkSoundsPlayer(getContext().getApplicationContext(), this);

        setHasOptionsMenu(true);
        eventsType = EventsManager.EventType.fromId(getArguments().getString(EVENTS_TYPE_KEY));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_events_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        notificationsList = eventsManager.getEventsByType(eventsType);
        eventsListAdapter = new EventsListAdapter(getActivity(), this);
        noNotificationsTextView = (TextView) view.findViewById(R.id.notificationsNoneAvailable);
        notificationsListView = (ListView) view.findViewById(R.id.notifications_user_info_lv);
        loadingProgress = (ProgressBar) view.findViewById(R.id.notifications_progressbar_pb);
        notificationsListView.setAdapter(eventsListAdapter);
        loadEvents(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.reload) {
            loadEvents(true);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBusManager.instance().register(this);
        if (!Utils.isOnline()) {
            EventBusManager.instance().postEvent(new NoInternetConnectionEvent());
        }
        if (eventsManager.getEventsTypeCount(eventsType) > 0) {
            notificationsList = eventsManager.getEventsByType(eventsType);
            reloadEventsList();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBusManager.instance().unRegister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        chunkSoundsPlayer.destroy();
    }

    @Override
    public void onPlayButtonClicked(Alarm alarm) {
        chunkSoundsPlayer.stop();
        eventsListAdapter.setPlayingAlarmId(alarm.getObjectId());
        eventsListAdapter.notifyDataSetChanged();
        AlarmManager.instance().getRecordedChunksSoundForAlarm(alarm.getObjectId(),
                new GetChunkSoundListener(this, chunkSoundsPlayer));
    }

    @Override
    public void onStopButtonClicked() {
        chunkSoundsPlayer.stop();
    }

    @Override
    public void onAlarmDetails(Alarm alarm) {
        Intent alarmIntent = new Intent(getActivity(), AlarmActivity.class);
        alarmIntent.putExtra(AlarmActivity.ALARM_ID_KEY, alarm.getObjectId());
        startActivity(alarmIntent);
    }

    @Override
    public void onIgnoreAlarm(Alarm alarm) {
        loadingProgress.setVisibility(View.VISIBLE);
        notificationsListView.setVisibility(View.GONE);
        AlarmManager.instance().ignoreAlarm(alarm.getObjectId(), new IgnoreAlarmListener(this));
    }

    @Override
    public void onAccept(GuardianInvitation invitation) {
        responseToInvitation(invitation, UserRelationStatus.ACCEPTED);
    }

    @Override
    public void onDecline(GuardianInvitation invitation) {
        responseToInvitation(invitation, UserRelationStatus.REJECTED);
    }

    @Override
    public void onCheckIn(CheckIn checkIn) {
        Intent checkInIntent = new Intent(getActivity(), LastCheckinActivity.class);
        checkInIntent.putExtra(LastCheckinActivity.KEY_MODEL_CHECKIN, checkIn.getObjectId());
        startActivity(checkInIntent);
    }

    private void loadEvents(boolean refresh) {
        if (Utils.isOnline()) {
            if (refresh || sLastUpdateTime + TIME_CACHE_MS < System.currentTimeMillis()) {
                loadingProgress.setVisibility(View.VISIBLE);
                notificationsListView.setVisibility(View.GONE);
                eventsManager.getEventsForUser(userManager.getUserModel().getObjectId(), new GetNotificationsListener(this, true));
            } else {
                loadingProgress.setVisibility(View.VISIBLE);
                notificationsListView.setVisibility(View.GONE);
                loadingProgress.setVisibility(View.GONE);
                eventsListAdapter.setData(notificationsList);
                if (eventsListAdapter.getCount() > 0) {
                    notificationsListView.setVisibility(View.VISIBLE);
                    noNotificationsTextView.setVisibility(View.GONE);
                } else {
                    noNotificationsTextView.setVisibility(View.VISIBLE);
                }
            }
        } else {
            PopDialog.showDialog(getActivity(),
                    getString(R.string.events_screen_dialog_error_youneedanactiveinternetconnection_message),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                            startActivity(intent);
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
        }
    }

    private void responseToInvitation(GuardianInvitation invitation, UserRelationStatus status) {
        loadingProgress.setVisibility(View.VISIBLE);
        eventsManager.responseToInvitation(invitation.getFromUser().getObjectId(),
                invitation.getToUser().getObjectId(), status,
                new RespondToInvitationListener(this));
    }

    public void reloadEventsList() {
        sLastUpdateTime = System.currentTimeMillis();
        eventsListAdapter.setData(notificationsList);
        if (eventsListAdapter.getCount() > 0) {
            notificationsListView.setVisibility(View.VISIBLE);
            noNotificationsTextView.setVisibility(View.GONE);
        } else {
            noNotificationsTextView.setVisibility(View.VISIBLE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onNewEventsReceived(NewEventsReceivedEvent event) {
        EventBus.getDefault().removeStickyEvent(NewEventsReceivedEvent.class);
        notificationsList = eventsManager.getEventsByType(eventsType);
        reloadEventsList();
    }

    @Override
    public void onStartPlaying() {
    }

    @Override
    public void onStopPlaying() {
        eventsListAdapter.setPlayingAlarmId(null);
        eventsListAdapter.notifyDataSetChanged();
    }

    private static class GetChunkSoundListener implements OnAlarmSoundsCallback {

        private WeakReference<EventsListFragment> weakReference;
        private ChunkSoundsPlayer chunkSoundsPlayer;

        GetChunkSoundListener(EventsListFragment fragment, ChunkSoundsPlayer chunkSoundsPlayer) {
            weakReference = new WeakReference<>(fragment);
            this.chunkSoundsPlayer = chunkSoundsPlayer;
        }

        @Override
        public void onSuccess(List<AlarmRecordingChunk> objects) {
            chunkSoundsPlayer.setChunkList(objects);
            chunkSoundsPlayer.play();
        }

        @Override
        public void onFailed(Error error) {
            EventsListFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            fragment.onStopButtonClicked();
            Toast.makeText(fragment.getContext(), error.getErrorMessageResId(), Toast.LENGTH_LONG).show();
        }
    }

    private static class RespondToInvitationListener implements OnResponseCallback {
        private WeakReference<EventsListFragment> weakReference;

        RespondToInvitationListener(EventsListFragment fragment) {
            weakReference = new WeakReference<>(fragment);
        }

        @Override
        public void onSuccess(ParseObject object) {
            EventsListFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            sLastUpdateTime = 0;
            fragment.loadEvents(true);
        }

        @Override
        public void onFailed(Error errorModel) {
            EventsListFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            fragment.loadingProgress.setVisibility(View.GONE);
            Toast.makeText(fragment.getActivity(), errorModel.getErrorMessageResId(), Toast.LENGTH_LONG).show();
        }
    }

    private static class GetNotificationsListener implements OnResponseListCallback {
        private WeakReference<EventsListFragment> weakReference;
        private boolean handleError = true;

        GetNotificationsListener(EventsListFragment fragment, boolean handleError) {
            weakReference = new WeakReference<>(fragment);
            this.handleError = handleError;
        }

        @Override
        public void onFailed(Error responseError) {
            EventsListFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            if (handleError) {
                PopDialog.showDialog(fragment.getActivity(),
                        fragment.getString(R.string.events_screen_dialog_error_unabletoretrieve_message));
            }
        }

        @Override
        public void onSuccess(Map<String, List<ParseObject>> objects) {
            EventsListFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            fragment.loadingProgress.setVisibility(View.GONE);
            fragment.notificationsList = fragment.eventsManager.getEventsByType(fragment.eventsType);
            fragment.reloadEventsList();
        }
    }

    private static class IgnoreAlarmListener implements OnResponseCallback {
        private WeakReference<EventsListFragment> weakReference;

        IgnoreAlarmListener(EventsListFragment fragment) {
            weakReference = new WeakReference<>(fragment);
        }

        @Override
        public void onSuccess(ParseObject object) {
            EventsListFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            sLastUpdateTime = 0;
            fragment.loadEvents(true);
        }

        @Override
        public void onFailed(Error errorModel) {
            EventsListFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            PopDialog.showDialog(fragment.getActivity(),
                    errorModel.getErrorMessage(fragment.getActivity()));
        }
    }

}
