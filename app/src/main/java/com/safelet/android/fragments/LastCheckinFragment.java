package com.safelet.android.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.safelet.android.R;
import com.safelet.android.global.PopDialog;
import com.safelet.android.global.Utils;
import com.safelet.android.interactors.CheckInManager;
import com.safelet.android.interactors.callbacks.OnResponseCallback;
import com.safelet.android.models.CheckIn;
import com.safelet.android.models.enums.NavigationMenu;
import com.safelet.android.models.event.NewNotificationEvent;
import com.safelet.android.utils.Error;
import com.safelet.android.utils.LogoutHelper;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;

/**
 * Last check in fragment
 * <p/>
 * Created by alin on 19.10.2015.
 */
public class LastCheckinFragment extends BaseFragment {
    private static final String KEY_MODEL_CONN = "KEY_MODEL_CONN";
    private static final String KEY_MODEL_CHECKIN = "KEY_MODEL_CHECKIN";

    private GoogleMap googleMap;
    private TextView checkInDescription;
    private TextView checkInTitle;
    private ImageView lastCheckInImageView;
    private MapView mapView;

    public static LastCheckinFragment newInstanceLastChecking(String userObjectId) {
        LastCheckinFragment fragment = new LastCheckinFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_MODEL_CONN, userObjectId);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static LastCheckinFragment newInstanceChecking(String checkInObjectId) {
        LastCheckinFragment fragment = new LastCheckinFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_MODEL_CHECKIN, checkInObjectId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_last_checkin, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkInTitle = (TextView) view.findViewById(R.id.lastCheckinTitle);
        checkInDescription = (TextView) view.findViewById(R.id.lastCheckinAddress);
        lastCheckInImageView = (ImageView) view.findViewById(R.id.lastCheckinImage);
        mapView = (MapView) view.findViewById(R.id.map_view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                // Retrieves google map and adjust its settings
                LastCheckinFragment.this.googleMap = googleMap;
                LastCheckinFragment.this.googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                LastCheckinFragment.this.googleMap.getUiSettings().setZoomControlsEnabled(false);
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    LastCheckinFragment.this.googleMap.setMyLocationEnabled(false);
                }
                loadCheckInFromServer();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
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
    protected void onNavigationDrawerItemSelected(NavigationMenu navigationMenu) {
        // do nothing
    }

    @Override
    protected void onViewNotificationDialogClicked(Context context, NewNotificationEvent event) {
        goToHomeScreenWithNotification(event);
    }

    @Override
    public int getTitleResId() {
        return R.string.checkin_title;
    }

    private void loadCheckInFromServer() {
        showLoading();
        if (getArguments().containsKey(KEY_MODEL_CONN)) {
            String userId = getArguments().getString(KEY_MODEL_CONN);
            CheckInManager.instance().getLastCheckInForUser(userId, new GetCheckInListener(this));
        } else {
            String checkInId = getArguments().getString(KEY_MODEL_CHECKIN);
            CheckInManager.instance().getCheckInById(checkInId, new GetCheckInListener(this));
        }
    }

    private void loadCheckIn(CheckIn checkin, String error) {
        if (checkin != null) {
            ParseGeoPoint location = checkin.getLocation();
            //get current address;
            String address = checkin.getLocationName();
            if (address != null && location != null && checkInDescription != null) {
                googleMap.clear();
                checkInDescription.setText(address);
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
                        .title(checkin.getUserName())
                        .snippet(address);
                googleMap.addMarker(markerOptions);
            } else if (address == null && location != null) {
                googleMap.clear();
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
                        .title(checkin.getUserName())
                        .snippet(getString(R.string.last_checkin_noaddressfound));
                googleMap.addMarker(markerOptions);
            }
            checkInDescription.setText(String.format(getString(R.string.last_checkin_chekedinlabel), Utils.getTimeAgo(checkin.getUpdatedAt().getTime(),
                    getString(R.string.last_checkin_chekedinjustnow))));
            checkInTitle.setText(checkin.getUserName());
            String userPicUrl = checkin.getUserImageUrl();
            if (!userPicUrl.isEmpty()) {
                Picasso.get().load(userPicUrl)
                        .error(R.drawable.generic_icon).into(lastCheckInImageView);
            }
            if (location != null) {
                centerMapInPosition(location);
            }
        } else {
            PopDialog.showDialog(getActivity(), getString(R.string.last_checkin_alert_screen_title),
                    error,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getActivity().finish();
                        }
                    });
        }
    }

    private void centerMapInPosition(ParseGeoPoint location) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),
                location.getLongitude()), 15));
    }

    private static class GetCheckInListener implements OnResponseCallback {
        private WeakReference<LastCheckinFragment> weakReference;

        GetCheckInListener(LastCheckinFragment fragment) {
            weakReference = new WeakReference<>(fragment);
        }

        @Override
        public void onSuccess(ParseObject object) {
            LastCheckinFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            fragment.loadCheckIn((CheckIn) object, null);
            fragment.hideLoading();
        }

        @Override
        public void onFailed(Error error) {
            LastCheckinFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            if (!LogoutHelper.handleExpiredSession(fragment.getActivity(), error)) {
                fragment.loadCheckIn(null, error == null ?
                        fragment.getString(R.string.last_checkin_no_checkin_msg) : error.getErrorMessage(fragment.getActivity()));
            }
            fragment.hideLoading();
        }
    }
}
