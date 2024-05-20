package com.safelet.android.interactors;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.RequiresPermission;
import androidx.core.content.ContextCompat;

import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.FenceClient;
import com.google.android.gms.awareness.fence.DetectedActivityFence;
import com.google.android.gms.awareness.fence.FenceQueryRequest;
import com.google.android.gms.awareness.fence.FenceQueryResponse;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.FenceStateMap;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.safelet.android.R;
import com.safelet.android.global.ApplicationSafelet;
import com.safelet.android.interactors.callbacks.LocationUpdateCallback;
import com.safelet.android.interactors.callbacks.OnResponseCallback;
import com.safelet.android.models.UserModel;
import com.safelet.android.models.event.NewLocationEvent;
import com.safelet.android.utils.Error;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class LocationManager implements GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = LocationManager.class.getSimpleName();

    private static final String LOCATION = "location";

    private static final String SINGLE_LOCATION = "single_location";

    private static final String PERIODIC_LOCATION = "periodic_location";

    private static final String FENCE = "fence";

    private static final int ACCEPTED_ACCURACY_IN_METERS = 500;

    private List<LocationUpdateCallback> locationListeners = new ArrayList<>();

    private GoogleApiClient googleApiClient;
    private ParseGeoPoint currentParseGeoPoint;
    private String currentAddress;

    private final FusedLocationProviderClient fusedLocationClient;
    private final LocationRequest locationRequest;

    private LocationRequest singleLocationRequest;
    private LocationRequest periodicLocationRequest;

    private final FenceClient fenceClient;

    private static LocationManager sInstance;

    private PendingIntent locationPendingIntent;
    private PendingIntent singleLocationPendingIntent;
    private PendingIntent periodicLocationPendingIntent;
    private PendingIntent fencePendingIntent;

    public static LocationManager getInstance() {
        if (sInstance == null) {
            sInstance = new LocationManager();
        }
        return sInstance;
    }

    @SuppressLint("WrongConstant")
    private LocationManager() {
        Context context = ApplicationSafelet.getContext();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(UpdateRates.UNKNOWN.getInterval());

        fenceClient = Awareness.getFenceClient(context);

        locationPendingIntent = PendingIntent.getBroadcast(ApplicationSafelet.getContext(), 0, new Intent(LOCATION), PendingIntent.FLAG_MUTABLE);
        singleLocationPendingIntent = PendingIntent.getBroadcast(ApplicationSafelet.getContext(), 0, new Intent(SINGLE_LOCATION), PendingIntent.FLAG_MUTABLE);
        periodicLocationPendingIntent = PendingIntent.getBroadcast(ApplicationSafelet.getContext(), 0, new Intent(PERIODIC_LOCATION), PendingIntent.FLAG_MUTABLE);
        fencePendingIntent = PendingIntent.getBroadcast(ApplicationSafelet.getContext(), 0, new Intent(FENCE), PendingIntent.FLAG_MUTABLE);

        context.registerReceiver(new LocationReceiver(), new IntentFilter(LOCATION));
        context.registerReceiver(new LocationReceiver(), new IntentFilter(SINGLE_LOCATION));
        context.registerReceiver(new LocationReceiver(), new IntentFilter(PERIODIC_LOCATION));
        context.registerReceiver(new ActivityFenceReceiver(), new IntentFilter(FENCE));

        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(ActivityRecognition.API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();

        locationListeners.add(new CurrentUserLocationUpdateListener());

        currentAddress = context.getString(R.string.alert_screen_unknownaddress_label);
    }

    public void start() {
        googleApiClient.connect();
    }

    public void stop() {
        stopLocationUpdate();
        stopAwarenessUpdate();
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    public void requestLocation() {
        if (googleApiClient.isConnected()) {
            singleLocationRequest = LocationRequest.create().setNumUpdates(1).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            fusedLocationClient.requestLocationUpdates(singleLocationRequest, singleLocationPendingIntent);
        } else {
            googleApiClient.connect();
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    public void requestPeriodicLocation(long interval) {
        if (googleApiClient.isConnected()) {
            periodicLocationRequest = LocationRequest.create().setInterval(interval).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            fusedLocationClient.requestLocationUpdates(periodicLocationRequest, periodicLocationPendingIntent);
        } else {
            googleApiClient.connect();
        }
    }

    public void stopPeriodicLocationUpdates() {
        if (periodicLocationRequest != null) {
            fusedLocationClient.removeLocationUpdates(periodicLocationPendingIntent);
        }
        periodicLocationRequest = null;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        try {
            fenceClient.queryFences(FenceQueryRequest.all()).addOnSuccessListener(new OnSuccessListener<FenceQueryResponse>() {
                @Override
                public void onSuccess(FenceQueryResponse fenceQueryResponse) {
                    if (fenceQueryResponse == null) {
                        return;
                    }
                    FenceStateMap fenceStateMap = fenceQueryResponse.getFenceStateMap();
                    if (fenceStateMap == null) {
                        return;
                    }
                    FenceState fenceState;
                    for (String fenceKey : fenceStateMap.getFenceKeys()) {
                        fenceState = fenceStateMap.getFenceState(fenceKey);
                        if (fenceState.getCurrentState() == FenceState.TRUE) {
                            setUpdateRate(UpdateRates.fromActivity(Integer.valueOf(fenceKey)));
                            break;
                        }
                    }

                }
            });
            startAwarenessUpdate();
            if (ContextCompat.checkSelfPermission(ApplicationSafelet.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                startLocationUpdate();
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "Error connecting on location updates service");
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    private void startLocationUpdate() {
        if (singleLocationRequest != null) {
            fusedLocationClient.requestLocationUpdates(singleLocationRequest, singleLocationPendingIntent);
            singleLocationRequest = null;
        }
        if (periodicLocationRequest != null) {
            fusedLocationClient.requestLocationUpdates(periodicLocationRequest, periodicLocationPendingIntent);
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationPendingIntent);
    }

    private void stopLocationUpdate() {
        if (periodicLocationRequest != null) {
            fusedLocationClient.removeLocationUpdates(periodicLocationPendingIntent);
            periodicLocationRequest = null;
        }
        fusedLocationClient.removeLocationUpdates(locationPendingIntent);
    }

    private void startAwarenessUpdate() {
        int[] activityFences = new int[]{DetectedActivity.IN_VEHICLE, DetectedActivity.ON_BICYCLE,
                DetectedActivity.ON_FOOT, DetectedActivityFence.STILL, DetectedActivityFence.WALKING, DetectedActivityFence.RUNNING};
        for (int fence : activityFences) {
            fenceClient.updateFences(new FenceUpdateRequest.Builder().addFence(String.valueOf(fence),
                    DetectedActivityFence.during(fence), fencePendingIntent).build());
        }
    }

    private void stopAwarenessUpdate() {
        int[] activityFences = new int[]{DetectedActivity.IN_VEHICLE, DetectedActivity.ON_BICYCLE,
                DetectedActivity.ON_FOOT, DetectedActivityFence.STILL, DetectedActivityFence.WALKING, DetectedActivityFence.RUNNING};
        for (int fence : activityFences) {
            fenceClient.updateFences(new FenceUpdateRequest.Builder().removeFence(String.valueOf(fence)).build());
        }
    }

    private void sendLocationReceivedCallback() {
        for (LocationUpdateCallback callback : locationListeners) {
            callback.onLocationReceived(currentParseGeoPoint);
        }
    }

    private void sendAddressReceivedCallback() {
        for (LocationUpdateCallback callback : locationListeners) {
            callback.onAddressReceived(currentAddress);
        }
    }

    private void setLocation(Location location) {
        ParseGeoPoint locationModel = new ParseGeoPoint();
        locationModel.setLatitude((float) location.getLatitude());
        locationModel.setLongitude((float) location.getLongitude());
        currentParseGeoPoint = locationModel;
        sendLocationReceivedCallback();
        retrieveCurrentAddress();
    }

    private void setLocationAddress(String address) {
        currentAddress = address;
        sendAddressReceivedCallback();
    }

    private void refreshLocationRequest() {
        if (googleApiClient.isConnected()) {
            if (ContextCompat.checkSelfPermission(ApplicationSafelet.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                startLocationUpdate();
            }
        } else {
            googleApiClient.connect();
        }
    }

    private void retrieveCurrentAddress() {
        new AddressTask(currentParseGeoPoint).execute();
    }

    private void setUpdateRate(UpdateRates rate) {
        locationRequest.setInterval(rate.getInterval());
        refreshLocationRequest();
    }

    /**
     * Add location update callback to listen for location update
     * <br/>
     * <b>NOTE: for each add location listener method, call {@link LocationManager#removeLocationListener(LocationUpdateCallback)}</b>
     *
     * @param locationUpdateCallback update location callback object
     */
    public void addLocationListener(LocationUpdateCallback locationUpdateCallback) {
        if (!locationListeners.contains(locationUpdateCallback)) {
            locationListeners.add(locationUpdateCallback);
        }
        if (!googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
            googleApiClient.connect();
        }
    }

    public void removeLocationListener(LocationUpdateCallback locationUpdateCallback) {
        locationListeners.remove(locationUpdateCallback);
        if (locationListeners.isEmpty()) {
            googleApiClient.disconnect();
        }
    }

    public ParseGeoPoint getCurrentLocation() {
        return currentParseGeoPoint;
    }

    public String getCurrentAddress() {
        return currentAddress;
    }

    private static class LocationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            LocationResult locationResult = LocationResult.extractResult(intent);

            if (locationResult != null) {
                Location location = locationResult.getLastLocation();
                Timber.i("Location = ".concat(location.toString()));

                // Firebase Long Event
                Bundle params = new Bundle();
                params.putString("latitude", String.valueOf(location.getLatitude()));
                params.putString("longitude", String.valueOf(location.getLongitude()));
                FirebaseAnalytics.getInstance(context).logEvent("Location", params);

                if (location.getAccuracy() < ACCEPTED_ACCURACY_IN_METERS && location.getLatitude() <= 90.0 && location.getLatitude() >= -90.0 &&
                        location.getLongitude() <= 180.0 && location.getLongitude() >= -180.0) {
                    // Only accurate locations are handled
                    // Crash reports were suggesting that some locations has longitude bigger/smaller than the max/min values
                    LocationManager.getInstance().setLocation(location);
                }
            }
        }
    }

    private static class ActivityFenceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            FenceState fenceState = FenceState.extract(intent);
            Timber.i("Fence type = " + fenceState.getFenceKey() + " fence state = " + fenceState.getCurrentState());

            // Firebase Long Event
            Bundle params = new Bundle();
            params.putString("type", fenceState.getFenceKey());
            params.putString("state", fenceState.toString());
            FirebaseAnalytics.getInstance(context).logEvent("Fence", params);

            switch (fenceState.getCurrentState()) {
                case FenceState.TRUE:
                    LocationManager.getInstance().setUpdateRate(UpdateRates.fromActivity(Integer.valueOf(fenceState.getFenceKey())));
                    break;
                case FenceState.FALSE:
                case FenceState.UNKNOWN:
                    if (fenceState.getPreviousState() == FenceState.TRUE) {
                        LocationManager.getInstance().setUpdateRate(UpdateRates.UNKNOWN);
                    }
                    break;
            }
        }
    }

    private static class CurrentUserLocationUpdateListener implements LocationUpdateCallback {

        @Override
        public void onLocationReceived(ParseGeoPoint location) {
            if (location != null && UserManager.instance().isUserLoggedIn()) {
                //Update location for user parse
                UserModel userModel = UserManager.instance().getUserModel();
                // Check if location is new for current user
                userModel.setLocation(location);
                UserManager.instance().saveUser(new OnResponseCallback() {

                    @Override
                    public void onSuccess(ParseObject object) {
                        // Send new location signal
                        EventBusManager.instance().postEvent(new NewLocationEvent());
                    }

                    @Override
                    public void onFailed(Error error) {
                    }
                });
            }
        }

        @Override
        public void onAddressReceived(String address) {
            if (!TextUtils.isEmpty(address) && UserManager.instance().isUserLoggedIn() &&
                    TextUtils.equals(ApplicationSafelet.getContext().getString(R.string.alert_screen_unknownaddress_label), address)) {
                //Update location for user parse
                UserModel userModel = UserManager.instance().getUserModel();
                userModel.setLocationName(address);
                UserManager.instance().saveUser(new OnResponseCallback() {

                    @Override
                    public void onSuccess(ParseObject object) {
                        // Send new location signal
                        EventBusManager.instance().postEvent(new NewLocationEvent());
                    }

                    @Override
                    public void onFailed(Error error) {
                    }
                });
            }
        }
    }

    private static class AddressTask extends AsyncTask<Void, Void, String> {

        private ParseGeoPoint location;

        private AddressTask(ParseGeoPoint location) {
            this.location = location;
        }

        @Override
        protected String doInBackground(Void... params) {
            return getAddress(location);
        }

        @Override
        protected void onPostExecute(String address) {
            super.onPostExecute(address);
            if (address != null) {
                LocationManager.getInstance().setLocationAddress(address);
            }
        }

        private String getAddress(ParseGeoPoint location) {
            Geocoder geocoder = new Geocoder(ApplicationSafelet.getContext(), Locale.getDefault());
            // Get the current location from the input parameter list
            // Create a list to contain the result address
            List<Address> addresses = new ArrayList<>();
            try {
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            } catch (IOException ignore) {
            }
            // If the reverse geocode returned an address
            if (!addresses.isEmpty()) {
                // Get the first address
                Address address = addresses.get(0);
                if (address.getMaxAddressLineIndex() >= 0) {
                    return address.getAddressLine(0);
                }
            }
            return null;
        }
    }

    public enum UpdateRates {
        IN_VEHICLE(DetectedActivity.IN_VEHICLE, 10 * DateUtils.SECOND_IN_MILLIS),
        ON_BICYCLE(DetectedActivity.ON_BICYCLE, 20 * DateUtils.SECOND_IN_MILLIS),
        ON_FOOT(DetectedActivity.ON_FOOT, 60 * DateUtils.SECOND_IN_MILLIS),
        RUNNING(DetectedActivity.RUNNING, 30 * DateUtils.SECOND_IN_MILLIS),
        STILL(DetectedActivity.STILL, DateUtils.DAY_IN_MILLIS),
        UNKNOWN(DetectedActivity.UNKNOWN, 4 * DateUtils.MINUTE_IN_MILLIS),
        WALKING(DetectedActivity.WALKING, 2 * DateUtils.MINUTE_IN_MILLIS);

        private int activity;

        private long interval;

        UpdateRates(int activity, long interval) {
            this.interval = interval;
            this.activity = activity;
        }

        private int getActivity() {
            return activity;
        }

        public long getInterval() {
            return interval;
        }

        public static UpdateRates fromActivity(int activity) {
            for (UpdateRates updateRates : values()) {
                if (updateRates.getActivity() == activity) {
                    return updateRates;
                }
            }
            return UNKNOWN;
        }
    }
}
