package com.safelet.android.utils;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import androidx.core.app.ActivityCompat;

public class PermissionManager {

    // Define a constant to identify the permission request
    public static final int REQUEST_BLUETOOTH_PERMISSION = 1;

    // Request BLUETOOTH_CONNECT permission
    public static void requestBluetoothPermission(Activity activity, PermissionCallback callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check API level before requesting permission (for Android 6.0+)
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN},
                    REQUEST_BLUETOOTH_PERMISSION
            );
        } else {
            // If the device is running an older version, consider permission granted.
            callback.onPermissionGranted();
        }
    }
}
