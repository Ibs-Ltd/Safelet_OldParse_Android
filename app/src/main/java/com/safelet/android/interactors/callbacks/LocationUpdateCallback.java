package com.safelet.android.interactors.callbacks;

import com.parse.ParseGeoPoint;

/**
 */
public interface LocationUpdateCallback {

    void onLocationReceived(ParseGeoPoint location);

    void onAddressReceived(String address);
}
