package com.safelet.android.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.safelet.android.R;
import com.safelet.android.activities.base.BaseActivity;
import com.safelet.android.callback.NavigationDrawerCallbacks;
import com.safelet.android.global.PopDialog;
import com.safelet.android.global.Utils;
import com.safelet.android.interactors.CheckInManager;
import com.safelet.android.interactors.EventBusManager;
import com.safelet.android.interactors.LocationManager;
import com.safelet.android.interactors.callbacks.LocationUpdateCallback;
import com.safelet.android.interactors.callbacks.OnResponseCallback;
import com.safelet.android.models.enums.NavigationMenu;
import com.safelet.android.models.event.NewNotificationEvent;
import com.safelet.android.models.event.NoInternetConnectionEvent;
import com.safelet.android.utils.Error;
import com.safelet.android.utils.LogoutHelper;

import java.lang.ref.WeakReference;

import timber.log.Timber;

/**
 * <b>Check-in handling screen</b>
 * <br/> Contains a map and a check-in functionality
 * that enables the user to send a message accompanied with it's current gps position if available
 * <br/> Handles network requests and ui updating
 *
 * @author Mihai Badea
 */
public class CheckInFragment extends BaseFragment implements View.OnClickListener, LocationUpdateCallback {

    private static final int REQUEST_CODE_PERMISSION = 7;

    private static final int ZOOM_LEVEL = 15;

    private NavigationDrawerCallbacks parentDrawerCallback;

    private final Handler updateLocationHandler = new Handler(Looper.myLooper());
    protected GoogleMap googleMap;
    protected EditText checkInMessageEditText;
    private MapView mapView;
    private View locationProgress;

    private boolean centered;

    private LocationManager locationManager;

    private ParseGeoPoint location;
    private String address;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.parentDrawerCallback = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException ignore) {
            //this should not happen
        }
        locationManager = LocationManager.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_checkin, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkInMessageEditText = view.findViewById(R.id.checkInMessageEt);
        mapView = view.findViewById(R.id.map_view);
        locationProgress = view.findViewById(R.id.location_progress);
        view.findViewById(R.id.checkinLocateIv).setOnClickListener(this);
        traverseViewForFixingBlackSurfaceView(view, 0);
        centered = false;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Timber.tag(BaseActivity.TAG).d("".concat("onMapReady"));
                // Retrieves google map and adjust its settings
                CheckInFragment.this.googleMap = googleMap;
                CheckInFragment.this.googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                CheckInFragment.this.googleMap.getUiSettings().setZoomControlsEnabled(false);
                updateLocation(location, address);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
        locationManager.addLocationListener(this);
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocation();
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_PERMISSION);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        // Registers to event bus
        EventBusManager.instance().register(this);
        // Check for gps status
        Utils.isGPSEnabled(getActivity());

        // Check if internet connection is active
        if (!Utils.isOnline()) {
            EventBusManager.instance().postEvent(new NoInternetConnectionEvent());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();

        updateLocationHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();

        locationManager.removeLocationListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION && ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocation();
        }
    }

    /**
     * Changes surfaceview background for not showing black bar after keyboard is off
     *
     * @param v     root for searching surfaceview
     * @param depth root depth search
     */
    private void traverseViewForFixingBlackSurfaceView(View v, int depth) {
        if (v instanceof SurfaceView) {
            SurfaceView sv = (SurfaceView) v;
            sv.setBackgroundColor(Color.TRANSPARENT);
        }

        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0, len = vg.getChildCount(); i < len; i++) {
                traverseViewForFixingBlackSurfaceView(vg.getChildAt(i), depth + 1);
            }
        }
    }

    @Override
    public int getTitleResId() {
        return R.string.checkin_title;
    }

    @Override
    public void onAlarmClicked() {
        parentDrawerCallback.onNavigationDrawerItemSelected(NavigationMenu.ALARM);
    }

    @Override
    protected void onNavigationDrawerItemSelected(NavigationMenu navigationMenu) {
        parentDrawerCallback.onNavigationDrawerItemSelected(navigationMenu);
    }

    @Override
    protected void onViewNotificationDialogClicked(Context context, NewNotificationEvent event) {
        handleNewNotificationEvent(event);
    }

    /**
     * Shows a marker for current user location
     */
    private void updateLocation(ParseGeoPoint location, String address) {
        if (googleMap == null) {
            Timber.tag(BaseActivity.TAG).d("".concat("googleMap null "));
            return;
        }
        if (location != null && address != null) {
            Timber.tag(BaseActivity.TAG).d("".concat("location and address not null "));
            // Address and location is available
            googleMap.clear();
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .title(getString(R.string.checkin_you_label))
                    .snippet(address);
            googleMap.addMarker(markerOptions);
        } else if (location != null) {
            Timber.tag(BaseActivity.TAG).d("".concat("location not null ---- "));
            // Only location is available
            googleMap.clear();
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .title(getString(R.string.checkin_you_label))
                    .snippet(getString(R.string.checkin_noaddressfound_label));
            googleMap.addMarker(markerOptions);
        }
        if (location != null && !centered) {
            Timber.tag(BaseActivity.TAG).d("".concat("location and centered condition"));
            locationProgress.setVisibility(View.GONE);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), ZOOM_LEVEL));
            centered = true;
        }
    }

    private void onCheckIn() {
        String message = checkInMessageEditText.getEditableText().toString();
        // Validation of checkin message
        if (!TextUtils.isEmpty(message)
                && ParseUser.getCurrentUser() != null
                && ParseUser.getCurrentUser().getObjectId() != null) {
            showLoading();
            CheckInManager.instance().createCheckInForUser(
                    ParseUser.getCurrentUser().getObjectId(),
                    locationManager.getCurrentLocation(),
                    locationManager.getCurrentAddress(),
                    message, new DispatchCheckInListener(this));
        } else if (TextUtils.isEmpty(message)) {
            PopDialog.showDialog(getActivity(),
                    getString(R.string.checkin_dialog_error_inserttextmessage));
        } else {
            PopDialog.showDialog(getActivity(),
                    getString(R.string.checkin_dialog_error_unk));
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.checkinLocateIv) {
            onCheckIn();
        }
    }

    @Override
    public void onLocationReceived(ParseGeoPoint location) {
        this.location = location;
        Timber.tag(BaseActivity.TAG).d("".concat("onLocationReceived: "));
        updateLocation(location, address);
    }

    @Override
    public void onAddressReceived(String address) {
        this.address = address;
        Timber.tag(BaseActivity.TAG).d("".concat("onAddressReceived: "));
        updateLocation(location, address);
    }

    private static class DispatchCheckInListener implements OnResponseCallback {

        private final WeakReference<CheckInFragment> weakReference;

        DispatchCheckInListener(CheckInFragment fragment) {
            weakReference = new WeakReference<>(fragment);
        }

        @Override
        public void onSuccess(ParseObject object) {
            final CheckInFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            fragment.hideLoading();
            // Checkin completed successfully
            PopDialog.showDialog(fragment.getActivity(), fragment.getString(R.string.checkin_dialog_title_success),
                    fragment.getString(R.string.checkin_dialog_message_success),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (fragment.checkInMessageEditText != null) {
                                fragment.checkInMessageEditText.getEditableText().clear();
                            }
                            fragment.onNavigationDrawerItemSelected(NavigationMenu.MY_CONNECTIONS);
                        }
                    });
        }

        @Override
        public void onFailed(Error error) {
            final CheckInFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            // Displays checkin action error
            fragment.hideLoading();
            if (!LogoutHelper.handleExpiredSession(fragment.getActivity(), error)) {
                PopDialog.showDialog(fragment.getActivity(),
                        fragment.getString(R.string.checkin_dialog_title_error_network),
                        error.getErrorMessage(fragment.getActivity()),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (fragment.checkInMessageEditText != null) {
                                    fragment.checkInMessageEditText.getEditableText().clear();
                                }
                            }
                        });
            }
        }
    }
}
