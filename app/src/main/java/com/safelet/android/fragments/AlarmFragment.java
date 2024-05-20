package com.safelet.android.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.safelet.android.R;
import com.safelet.android.activities.DisableAlarmActivity;
import com.safelet.android.activities.base.BaseActivity;
import com.safelet.android.adapters.MessageAdapter;
import com.safelet.android.callback.NavigationDrawerCallbacks;
import com.safelet.android.global.ApplicationSafelet;
import com.safelet.android.global.EmergencyNumberUtil;
import com.safelet.android.global.PopDialog;
import com.safelet.android.global.Utils;
import com.safelet.android.interactors.AlarmManager;
import com.safelet.android.interactors.EventBusManager;
import com.safelet.android.interactors.EventsManager;
import com.safelet.android.interactors.LocationManager;
import com.safelet.android.interactors.MessageManager;
import com.safelet.android.interactors.PhoneContactsManager;
import com.safelet.android.interactors.UserManager;
import com.safelet.android.interactors.callbacks.LocationUpdateCallback;
import com.safelet.android.interactors.callbacks.OnAlarmParticipantsCallback;
import com.safelet.android.interactors.callbacks.OnAlarmSoundsCallback;
import com.safelet.android.interactors.callbacks.OnResponseCallback;
import com.safelet.android.interactors.utils.ChunkSoundsPlayer;
import com.safelet.android.interactors.utils.IconGenerator;
import com.safelet.android.models.Alarm;
import com.safelet.android.models.AlarmRecordingChunk;
import com.safelet.android.models.ContactModel;
import com.safelet.android.models.Message;
import com.safelet.android.models.StopReason;
import com.safelet.android.models.User;
import com.safelet.android.models.UserModel;
import com.safelet.android.models.enums.NavigationMenu;
import com.safelet.android.models.event.AlarmJoinEvent;
import com.safelet.android.models.event.AlarmStopEvent;
import com.safelet.android.models.event.AlarmUpdateEvent;
import com.safelet.android.models.event.ChunkFileReceivedEvent;
import com.safelet.android.models.event.FirebaseInitializeEvent;
import com.safelet.android.models.event.FirebaseMessageEvent;
import com.safelet.android.models.event.LastChunkFileReceivedEvent;
import com.safelet.android.models.event.NewNotificationEvent;
import com.safelet.android.models.event.ParticipantsNotifiedEvent;
import com.safelet.android.models.event.ParticipantsReceivedEvent;
import com.safelet.android.services.RecordSoundService;
import com.safelet.android.utils.Error;
import com.safelet.android.utils.LogoutHelper;
import com.safelet.android.views.listener.EditorTextWatcher;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class AlarmFragment extends BaseFragment implements View.OnClickListener, LocationUpdateCallback
        , ChunkSoundsPlayer.OnChunkPlayListener,
        SlidingUpPanelLayout.PanelSlideListener, KeyboardVisibilityEventListener {

    private static final String ALARM_ID_EXTRA_KEY = "af.alarmId.key";
    private static final int REQUEST_CODE_DISABLE_ALARM = 139;
    private static final int UPDATE_ALARM_INTERVAL = 2 * 1000; // 2 seconds
    private static final long UPDATE_LOCATION_INTERVAL = 5 * DateUtils.SECOND_IN_MILLIS;
    private static final int ZOOM_LEVEL = 13;
    private static final int CAMERA_UPDATE_PADDING_DP = 110;

    private static final int REQUEST_CODE_LOCATION = 23;
    private static final int REQUEST_CODE_RECORD_AUDIO = 7;

    private final UserManager userManager = UserManager.instance();
    private final AlarmManager alarmManager = AlarmManager.instance();
    private ChunkSoundsPlayer chunkSoundsPlayer;
    private LocationManager locationManager;

    private Handler updateAlarmHandler = new Handler(Looper.getMainLooper());
    private Handler updateChunksHandler = new Handler(Looper.getMainLooper());

    private NavigationDrawerCallbacks parentDrawerCallback;

    protected GoogleMap googleMap;

    private IconGenerator iconGenerator;

    private SlidingUpPanelLayout slidingUpPanelLayout;

    private View myAlarmBottomLayout;
    private View onMyWayBottomLayout;

    private Button onMyWayButton;
    private Button playImageButton;
    private Button disableAlarmButton;
    private MapView mapView;

    private EditText editor;
    private ImageButton send;

    private View locationProgress;

    private MessageAdapter messageAdapter;

    private Marker currentPositionMarker;
    private List<Marker> guardianMarkers = new ArrayList<>();

    protected Alarm currentActiveAlarm;

    private ParseGeoPoint location;
    private boolean centered;

    private boolean isOwnAlarm;

    public static AlarmFragment newInstance() {
        return new AlarmFragment();
    }

    public static AlarmFragment newInstance(String alarmId) {
        AlarmFragment fragment = new AlarmFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ALARM_ID_EXTRA_KEY, alarmId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            parentDrawerCallback = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Fragment should be inflated in activities that implement NavigationDrawerCallbacks");
        }
        locationManager = LocationManager.getInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ALARM_ID_EXTRA_KEY)) {
            String alarmKey = getArguments().getString(ALARM_ID_EXTRA_KEY);
            currentActiveAlarm = EventsManager.instance().getAlarmById(alarmKey);
        }
        if (currentActiveAlarm == null && alarmManager.isActiveAlarm()) {
            currentActiveAlarm = alarmManager.getActiveAlarm();
        }

        chunkSoundsPlayer = new ChunkSoundsPlayer(getActivity(), this);

        removeStickyEvents(); // we don't care for old events if the fragment was recreated
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Loads fragment layout from xml
        return inflater.inflate(R.layout.fragment_alert, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Performs google map initialization
        MapsInitializer.initialize(getActivity());

        slidingUpPanelLayout = view.findViewById(R.id.sliding_panel_layout);
        slidingUpPanelLayout.addPanelSlideListener(this);

        locationProgress = view.findViewById(R.id.location_progress);

        KeyboardVisibilityEvent.registerEventListener(getActivity(), this);

        messageAdapter = new MessageAdapter(getContext());

        ListView messages = view.findViewById(R.id.messages_list);
        messages.setAdapter(messageAdapter);

        send = view.findViewById(R.id.send);
        send.setEnabled(false);
        send.setOnClickListener(this);
        editor = view.findViewById(R.id.editor);
        editor.addTextChangedListener(new EditorTextWatcher(send));

        TextView callEmergencyText = view.findViewById(R.id.call_emergency);
        callEmergencyText.setText(getString(R.string.call_emergency, EmergencyNumberUtil.getEmergencyNumber(userManager.getPhoneNumber())));
        callEmergencyText.setOnClickListener(this);

        myAlarmBottomLayout = view.findViewById(R.id.my_alarm_bottom_layout);
        disableAlarmButton = view.findViewById(R.id.disable_alarm);
        disableAlarmButton.setOnClickListener(this);
        onMyWayBottomLayout = view.findViewById(R.id.on_my_way_bottom_layout);
        playImageButton = view.findViewById(R.id.play_sound);
        playImageButton.setOnClickListener(this);
        onMyWayButton = view.findViewById(R.id.on_my_way_button);
        onMyWayButton.setOnClickListener(this);

        isOwnAlarm = alarmManager.isActiveAlarm() && alarmManager.getActiveAlarm().equals(currentActiveAlarm);

        Timber.d(BaseActivity.TAG.concat("isOwnAlarm ").concat(String.valueOf(isOwnAlarm)));
        if (isOwnAlarm) {
            myAlarmBottomLayout.setVisibility(View.VISIBLE);
            alarmView.setIsRecording(true);
        } else {
            onMyWayBottomLayout.setVisibility(View.VISIBLE);
            onMyWayButton.setEnabled(false);
            playImageButton.setEnabled(false);
            AlarmManager.instance().getParticipantsForAlarm(currentActiveAlarm.getObjectId()
                    , new GetParticipantsListener(false));
            updateChunkSoundsWithDelay(0);
        }
        mapView = view.findViewById(R.id.map_view);

        MessageManager.instance().authenticate();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                // Retrieves google map and adjust its settings
                AlarmFragment.this.googleMap = googleMap;
                AlarmFragment.this.googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                AlarmFragment.this.googleMap.getUiSettings().setZoomControlsEnabled(false);
                AlarmFragment.this.iconGenerator = new IconGenerator(getContext());
                updateAlarm();
                if (isOwnAlarm && location != null) {
                    addCurrentLocationMarker(location, getString(R.string.your_string));
                    centered = centerInMap(location);
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
        if (!currentActiveAlarm.isAlarmActive()) {
            // in case that the app is in background and user open the app from recents and
            // meanwhile the alarm is stopped
            getActivity().finish();
            return;
        }
        locationManager.addLocationListener(this);
        if (ContextCompat.checkSelfPermission(getContext()
                , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestPeriodicLocation(UPDATE_LOCATION_INTERVAL);
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                    , REQUEST_CODE_LOCATION);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("".concat(BaseActivity.TAG).concat("AlarmFragment ").concat("onResume"));
        mapView.onResume();
        Utils.isGPSEnabled(getActivity());
        // Updates navigation bar color
        setAlertMode(alarmManager.isActiveAlarm());
        if (currentPositionMarker == null) {
            showLoading();
        }

        Timber.d("".concat(BaseActivity.TAG).concat("isOwnAlarm ").concat(String.valueOf(isOwnAlarm)));
        if (isOwnAlarm && ContextCompat.checkSelfPermission(getContext()
                , Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_RECORD_AUDIO);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
        locationManager.removeLocationListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (currentActiveAlarm != null) {
            MessageManager.instance().unSubscribe(currentActiveAlarm.getObjectId());
        }

        mapView.onDestroy();

        stopUpdateAlarm();
        stopUpdateChunkSounds();
        chunkSoundsPlayer.stop();
        chunkSoundsPlayer.destroy();

        currentPositionMarker = null;

        if (guardianMarkers != null) {
            guardianMarkers.clear();
        }
        if (googleMap != null) {
            googleMap.clear();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Timber.d("".concat(BaseActivity.TAG).concat("onRequestPermissionsResult "));
        if (requestCode == REQUEST_CODE_LOCATION && ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Timber.d("".concat(BaseActivity.TAG).concat("onRequestPermissionsResult ").concat("ACCESS_FINE_LOCATION"));
            locationManager.requestPeriodicLocation(UPDATE_LOCATION_INTERVAL);
        } else if (requestCode == REQUEST_CODE_RECORD_AUDIO && ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            Timber.d("".concat(BaseActivity.TAG).concat("onRequestPermissionsResult ").concat("RECORD_AUDIO"));
            RecordSoundService.startRecording(getContext());
        } else {
            Timber.d("".concat(BaseActivity.TAG).concat("onRequestPermissionsResult ").concat("else"));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onNavigationDrawerItemSelected(NavigationMenu navigationMenu) {
        parentDrawerCallback.onNavigationDrawerItemSelected(navigationMenu);
    }

    @Override
    protected void onDismissNotificationDialogClicked(NewNotificationEvent event) {
        if (event.getNotificationType().toString().startsWith(NewNotificationEvent.NotificationType.STOP_ALARM.toString()) &&
                event.getObjectId().equalsIgnoreCase(currentActiveAlarm.getObjectId())) {
            stopUpdateAlarm();
            parentDrawerCallback.onNavigationDrawerItemSelected(NavigationMenu.MY_CONNECTIONS);
        }
    }

    @Override
    public int getTitleResId() {
        return R.string.last_checkin_alert_screen_title;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.on_my_way_button) {
            onMyWayAction();
        } else if (v.getId() == R.id.disable_alarm) {
            disableAlarm();
        } else if (v.getId() == R.id.play_sound) {
            if (!chunkSoundsPlayer.isPlaying()) {
                chunkSoundsPlayer.play();
            } else {
                chunkSoundsPlayer.stop();
            }
        } else if (v.getId() == R.id.call_emergency) {
            callEmergency();
        } else if (v.getId() == R.id.send) {
            MessageManager.instance().sendMessage(currentActiveAlarm.getObjectId()
                    , editor.getText().toString());
            editor.setText("");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_DISABLE_ALARM && resultCode == Activity.RESULT_OK) {
            disableAlarmButton.setEnabled(false);
            stopUpdateAlarm();
            showLoading();
            MessageManager.instance().unSubscribe(currentActiveAlarm.getObjectId());
            alarmManager.disableCurrentAlarm((StopReason.Reason) data.getSerializableExtra(DisableAlarmActivity.KEY_REASON),
                    data.getStringExtra(DisableAlarmActivity.KEY_DESCRIPTION), new DisableAlarmListener());
        }
    }

    // 97239 01478
    // 95120 74305

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
    }

    @Override
    public void onPanelStateChanged(final View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
        if (newState == SlidingUpPanelLayout.PanelState.ANCHORED || newState == SlidingUpPanelLayout.PanelState.COLLAPSED
                || newState == SlidingUpPanelLayout.PanelState.HIDDEN) {
            Utils.hideKeyboard(getActivity());
        }
    }

    @Override
    public void onVisibilityChanged(boolean isOpen) {
        if (isOpen) {
            slidingUpPanelLayout.post(new Runnable() {
                @Override
                public void run() {
                    slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                }
            });
        }
    }

    private void startUpdateAlarmWithDelay(long delayInMilliseconds) {
        updateAlarmHandler.postDelayed(new UpdateAlarmRunnable(this), delayInMilliseconds);
    }

    private void stopUpdateAlarm() {
        updateAlarmHandler.removeCallbacksAndMessages(null);
    }

    private void callEmergency() {
        Uri callUri = Uri.parse("tel:" + EmergencyNumberUtil.getEmergencyNumber(UserManager.instance().getPhoneNumber()));
        Intent callIntent = new Intent(Intent.ACTION_DIAL, callUri);
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(callIntent);

        alarmManager.notifyParticipantsDidCallEmergency(currentActiveAlarm.getObjectId(),
                new NotifyParticipantsListener());
    }

    /**
     * @param location null to center map to fit all participants, valid location to center with zoom in location
     */
    private boolean centerInMap(ParseGeoPoint location) {
        if (googleMap == null) {
            return false;
        }
        if (location != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), ZOOM_LEVEL));
            locationProgress.setVisibility(View.GONE);
            return true;
        } else if (guardianMarkers.size() > 0 || currentPositionMarker != null) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            if (currentPositionMarker != null) {
                builder.include(currentPositionMarker.getPosition());
            }
            for (Marker marker : guardianMarkers) {
                builder.include(marker.getPosition());
            }
            int padding = Utils.pixelsFromDp(CAMERA_UPDATE_PADDING_DP);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), padding);
            googleMap.animateCamera(cameraUpdate);
            locationProgress.setVisibility(View.GONE);
            return true;
        }
        return false;
    }

    private void disableAlarm() {
        if (!Utils.isOnline()) {
            // Doesn't update the alarm if the internet is not available
            PopDialog.showDialog(getActivity(), getString(R.string.alert_screen_dialog_title), getString(R.string
                    .alert_screen_dialog_message_error_network));
            return;
        }

        startActivityForResult(new Intent(getActivity(), DisableAlarmActivity.class), REQUEST_CODE_DISABLE_ALARM);
    }

    private void stopRecording() {
        Intent recordingIntent = new Intent(getActivity(), RecordSoundService.class);
        getActivity().stopService(recordingIntent);
        alarmView.setNotPlayback();
    }

    private void onMyWayAction() {
        if (!Utils.isOnline()) {
            // Doesn't update the alarm if the internet is not available
            PopDialog.showDialog(getActivity(), getString(R.string.alert_screen_dialog_title),
                    getString(R.string.alert_screen_dialog_message_error_network));
            return;
        }
        showLoading();
        onMyWayButton.setEnabled(false);
        alarmManager.joinAlarm(userManager.getUserId(), currentActiveAlarm.getObjectId(), new AlarmJoinListener());
    }

    private void refreshAlarm() {
        AlarmManager.instance().updateAlarmFromServer(currentActiveAlarm, new UpdateAlarmListener());
    }

    private void updateAlarm() {
        if (currentActiveAlarm.isAlarmActive()) {
            if (!isOwnAlarm) {
                addAlarmLocationMarker(currentActiveAlarm);
                if (!centered) {
                    centered = centerInMap(currentActiveAlarm.getAlertLocation());
                }
            }
            AlarmManager.instance().getParticipantsForAlarm(currentActiveAlarm.getObjectId(), new GetParticipantsListener(true));
            hideLoading();
        } else {
            stopUpdateAlarm();
            if (getActivity() != null)
                PopDialog.showDialog(getActivity(),
                        getString(R.string.alert_screen_alarm_stopped_message),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
        }
    }

    private void addAlarmLocationMarker(Alarm alarm) {
        ParseGeoPoint userLocation = alarm.getUser().getLocation();
        if (userLocation != null) { // maybe this should not happen
            addCurrentLocationMarker(alarm.getUser().getLocation(), alarm.getUser().getName());
        }
    }

    private void addCurrentLocationMarker(ParseGeoPoint location, String title) {
        Drawable pin = DrawableCompat.wrap(ContextCompat.getDrawable(getActivity(), R.drawable.ic_pin));
        DrawableCompat.setTint(pin, ContextCompat.getColor(getActivity(), R.color.pin_red));

        MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()));

        if (currentPositionMarker != null) {
            currentPositionMarker.remove();
        }
        currentPositionMarker = googleMap.addMarker(markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(pin, title))));
    }

    /**
     * @param delayInMilliseconds 0 to get the list with all chunks, > 0 to get last chunk file with delay
     */
    private void updateChunkSoundsWithDelay(long delayInMilliseconds) {
        if (delayInMilliseconds == 0) {
            AlarmManager.instance().getRecordedChunksSoundForAlarm(currentActiveAlarm.getObjectId(), new GetChunkSoundListener());
        } else {
            updateChunksHandler.postDelayed(new UpdateChunksRunnable(this), delayInMilliseconds);
        }
    }

    private void updateChunkSounds() {
        AlarmManager.instance().getLastRecordedChunkSoundForAlarm(currentActiveAlarm.getObjectId(), new GetLastChunkFileListener());
    }

    private void stopUpdateChunkSounds() {
        updateChunksHandler.removeCallbacksAndMessages(null);
    }

    private void updateParticipants(List<UserModel> participants) {
        if (googleMap == null) {
            return;
        }
        for (Marker marker : guardianMarkers) {
            marker.remove();
        }
        guardianMarkers.clear();

        if (participants != null) {
            for (int i = 0; i < participants.size(); i++) {
                UserModel participant = participants.get(i);
                ParseGeoPoint participantLocation = participant.getLocation();
                if (participantLocation != null) {
                    String title = participant.getName();
                    if (participant.getObjectId().equals(userManager.getUserId())) {
                        title = ApplicationSafelet.getContext().getString(R.string.your_string);
                    }

                    //Displays participant marker only if it has location available

                    Drawable pin = DrawableCompat.wrap(ContextCompat.getDrawable(getActivity(), R.drawable.ic_pin));
                    DrawableCompat.setTint(pin, ContextCompat.getColor(getActivity(), R.color.pin_green));

                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(new LatLng(participantLocation.getLatitude(), participantLocation.getLongitude()));
                    Marker marker = googleMap.addMarker(markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(pin, title))));
                    guardianMarkers.add(marker);
                }
            }
        }
        startUpdateAlarmWithDelay(UPDATE_ALARM_INTERVAL);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onAlarmJoined(AlarmJoinEvent event) {
        EventBus.getDefault().removeStickyEvent(AlarmJoinEvent.class);
        if (event.getError() == null) {
            refreshAlarm();
        } else {
            if (!LogoutHelper.handleExpiredSession(getActivity(), event.getError())) {
                PopDialog.showDialog(getActivity(), getString(R.string.alert_screen_dialog_title),
                        event.getError().getErrorMessage(getActivity()));
            }
            onMyWayButton.setEnabled(true);
        }

        hideLoading();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onAlarmStopped(AlarmStopEvent event) {
        EventBus.getDefault().removeStickyEvent(AlarmStopEvent.class);
        if (event.getError() == null) {
            stopRecording();
            EventBusManager.instance().postEvent(currentActiveAlarm);
            LocationManager.getInstance().stopPeriodicLocationUpdates();
            parentDrawerCallback.onNavigationDrawerItemSelected(NavigationMenu.MY_CONNECTIONS);
        } else {
            disableAlarmButton.setEnabled(true);
            if (!LogoutHelper.handleExpiredSession(getActivity(), event.getError())) {
                Toast.makeText(getActivity(), getString(R.string.alert_screen_dialog_message_error_network), Toast.LENGTH_SHORT).show();
            }
        }

        hideLoading();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onAlarmUpdated(AlarmUpdateEvent event) {
        EventBus.getDefault().removeStickyEvent(AlarmUpdateEvent.class);
        if (event.getError() == null) {
            currentActiveAlarm = (Alarm) event.getResult();
            updateAlarm();
        } else {
            if (!LogoutHelper.handleExpiredSession(getActivity(), event.getError())) {
                startUpdateAlarmWithDelay(UPDATE_ALARM_INTERVAL);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onChunkFilesReceived(ChunkFileReceivedEvent event) {
        EventBus.getDefault().removeStickyEvent(ChunkFileReceivedEvent.class);
        if (event.getError() == null) {
            if (event.getResults().size() > 0) {
                chunkSoundsPlayer.setChunkList(event.getResults());
                playImageButton.setEnabled(true);
            }
            updateChunkSoundsWithDelay(UPDATE_ALARM_INTERVAL);
        } else {
            if (!LogoutHelper.handleExpiredSession(getActivity(), event.getError())) {
                updateChunkSoundsWithDelay(0);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onLastChunkFileReceived(LastChunkFileReceivedEvent event) {
        EventBus.getDefault().removeStickyEvent(LastChunkFileReceivedEvent.class);
        if (event.getResult() != null) {
            chunkSoundsPlayer.addChunk((AlarmRecordingChunk) event.getResult());
            playImageButton.setEnabled(true);
        }
        if (!LogoutHelper.handleExpiredSession(getActivity(), event.getError())) {
            updateChunkSoundsWithDelay(UPDATE_ALARM_INTERVAL);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onParticipantsReceived(ParticipantsReceivedEvent event) {
        EventBus.getDefault().removeStickyEvent(ParticipantsReceivedEvent.class);
        if (event.getError() == null) {
            if (currentActiveAlarm.isAlarmActive()) {
                if (event.isUpdateParticipants()) {
                    updateParticipants(event.getParticipants());
                } else {
                    // check if current user is participant to this alarm
                    boolean joinedToAlarm = false;
                    for (UserModel model : event.getParticipants()) {
                        if (model.getObjectId().equals(userManager.getUserId())) {
                            joinedToAlarm = true;
                            break;
                        }
                    }
                    if (!joinedToAlarm) {
                        onMyWayButton.setEnabled(true);
                    }
                }
            }
        } else if (!LogoutHelper.handleExpiredSession(getActivity(), event.getError())) {
            if (event.isUpdateParticipants()) {
                updateParticipants(null);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onParticipantsNotified(ParticipantsNotifiedEvent event) {
        EventBus.getDefault().removeStickyEvent(ParticipantsNotifiedEvent.class);
        if (event.getError() == null) {
            PopDialog.showDialog(getActivity(),
                    getString(R.string.dialog_title_success),
                    getString(R.string.participants_notified_message), null);
        } else {
            LogoutHelper.handleExpiredSession(getActivity(), event.getError());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onFirebaseInitialized(FirebaseInitializeEvent event) {
        EventBus.getDefault().removeStickyEvent(FirebaseInitializeEvent.class);
        MessageManager.instance().subscribe(currentActiveAlarm.getObjectId());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFirebaseMessage(FirebaseMessageEvent event) {
        UserManager userManager = UserManager.instance();
        Message message = event.getMessage();
        User sender = message.getSender();
        if (TextUtils.equals(sender.getId(), userManager.getUserId())) {
            message.setType(Message.Type.OUTGOING);
            sender.setName(getString(R.string.your_string));
        } else {
            message.setType(Message.Type.INCOMING);
            ContactModel contactModel = PhoneContactsManager.instance().getContact(sender.getNumber());
            if (contactModel != null) {
                sender.setName(contactModel.getName());
                String photo = contactModel.getPhotoUri() != null ? contactModel.getPhotoUri().toString() : null;
                if (!TextUtils.isEmpty(photo)) {
                    sender.setImage(photo);
                }
            }
        }

        messageAdapter.insert(message, 0);

        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
    }

    @Override
    protected void onViewNotificationDialogClicked(Context context, NewNotificationEvent event) {
        if (event.getObjectId().equals(currentActiveAlarm.getObjectId())) {
            return;
        }
        handleNewNotificationEvent(event);
    }

    @Override
    public void onStartPlaying() {
        playImageButton.setText(R.string.stop_audio);
    }

    @Override
    public void onStopPlaying() {
        playImageButton.setText(R.string.play_audio);
    }

    private void removeStickyEvents() {
        EventBus.getDefault().removeStickyEvent(AlarmJoinEvent.class);
        EventBus.getDefault().removeStickyEvent(AlarmStopEvent.class);
        EventBus.getDefault().removeStickyEvent(AlarmUpdateEvent.class);
        EventBus.getDefault().removeStickyEvent(ChunkFileReceivedEvent.class);
        EventBus.getDefault().removeStickyEvent(LastChunkFileReceivedEvent.class);
        EventBus.getDefault().removeStickyEvent(ParticipantsReceivedEvent.class);
        EventBus.getDefault().removeStickyEvent(ParticipantsNotifiedEvent.class);
        EventBus.getDefault().removeStickyEvent(FirebaseInitializeEvent.class);
    }

    @Override
    public void onLocationReceived(ParseGeoPoint location) {
        this.location = location;
        if (isOwnAlarm && googleMap != null) {
            addCurrentLocationMarker(location, getString(R.string.your_string));
            if (!centered) {
                centered = centerInMap(location);
            }
        }
    }

    @Override
    public void onAddressReceived(String address) {
    }

    private static class AlarmJoinListener implements OnResponseCallback {

        @Override
        public void onSuccess(ParseObject object) {
            EventBus.getDefault().postSticky(new AlarmJoinEvent());
        }

        @Override
        public void onFailed(Error error) {
            EventBus.getDefault().postSticky(new AlarmJoinEvent(error));
        }
    }

    private static class DisableAlarmListener implements OnResponseCallback {

        @Override
        public void onSuccess(ParseObject object) {
            EventBus.getDefault().postSticky(new AlarmStopEvent());
        }

        @Override
        public void onFailed(Error error) {
            EventBus.getDefault().postSticky(new AlarmStopEvent(error));
        }
    }

    private static class GetLastChunkFileListener implements OnResponseCallback {

        @Override
        public void onSuccess(ParseObject object) {
            EventBus.getDefault().postSticky(new LastChunkFileReceivedEvent(object));
        }

        @Override
        public void onFailed(Error error) {
            EventBus.getDefault().postSticky(new LastChunkFileReceivedEvent(error));
        }
    }

    private static class GetChunkSoundListener implements OnAlarmSoundsCallback {

        @Override
        public void onSuccess(List<AlarmRecordingChunk> objects) {
            EventBus.getDefault().postSticky(new ChunkFileReceivedEvent(objects));
        }

        @Override
        public void onFailed(Error error) {
            EventBus.getDefault().postSticky(new ChunkFileReceivedEvent(error));
        }
    }

    private static class GetParticipantsListener implements OnAlarmParticipantsCallback {

        /*
        This member is used to know if we should update the participants or not.
        - true: to update the participants markers from the map
        - false: only to check if current user is joined already or not the this alarm
         */
        private boolean updateParticipants = true;

        GetParticipantsListener(boolean updateParticipants) {
            this.updateParticipants = updateParticipants;
        }

        @Override
        public void onSuccess(List<UserModel> objects) {
            EventBus.getDefault().postSticky(new ParticipantsReceivedEvent(objects, updateParticipants));
        }

        @Override
        public void onFailed(Error error) {
            EventBus.getDefault().postSticky(new ParticipantsReceivedEvent(error, updateParticipants));
        }
    }

    private static class UpdateAlarmListener implements OnResponseCallback {

        @Override
        public void onSuccess(ParseObject object) {
            EventBus.getDefault().postSticky(new AlarmUpdateEvent(object));
        }

        @Override
        public void onFailed(Error error) {
            EventBus.getDefault().postSticky(new AlarmUpdateEvent(error));
        }
    }

    private static class NotifyParticipantsListener implements OnResponseCallback {

        @Override
        public void onSuccess(ParseObject object) {
            EventBus.getDefault().postSticky(new ParticipantsNotifiedEvent());
        }

        @Override
        public void onFailed(Error error) {
        }
    }

    private static class UpdateAlarmRunnable implements Runnable {

        private WeakReference<AlarmFragment> weakReference;

        UpdateAlarmRunnable(AlarmFragment fragment) {
            weakReference = new WeakReference<>(fragment);
        }

        @Override
        public void run() {
            AlarmFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            fragment.refreshAlarm();
        }
    }

    private static class UpdateChunksRunnable implements Runnable {

        private WeakReference<AlarmFragment> weakReference;

        UpdateChunksRunnable(AlarmFragment fragment) {
            weakReference = new WeakReference<>(fragment);
        }

        @Override
        public void run() {
            AlarmFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            fragment.updateChunkSounds();
        }
    }

}