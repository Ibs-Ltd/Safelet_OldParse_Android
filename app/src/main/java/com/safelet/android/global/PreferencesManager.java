package com.safelet.android.global;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {
    // preferences name
    private static final String SAFELET_SHARED_PREFERENCES_KEY = "safelet.pm.name";
    // preference keys
    public static final String LOGIN_EMAIL_KEY = "pm.loginEmail.key";
    public static final String DEVICE_MANUFACTURER_NAME = "pm.deviceInfoManufactorerName.key";
    public static final String DEVICE_MODEL_NUMBER = "pm.deviceInfoModelNumber.key";
    public static final String DEVICE_HARDWARE_REVISION = "pm.deviceInfoHardwareRevision.key";
    public static final String DEVICE_FIRMWARE_REVISION = "pm.deviceInfoFirmwareRevision.key";
    public static final String DEVICE_FIRMWARE_VERSION_CODE = "pm.deviceInfoFirmwareVersionCode.key";
    public static final String BATTERY_LEVEL_LOW = "pm.batteryLevelLow.key";
    public static final String BATTERY_LEVEL_DANGEROUS = "pm.batteryLevelDangerous.key";

    private SharedPreferences preferences;

    private static PreferencesManager sInstance;

    public static PreferencesManager instance(Context context) {
        if (sInstance == null) {
            sInstance = new PreferencesManager(context);
        }
        return sInstance;
    }

    private PreferencesManager(Context context) {
        preferences = context.getSharedPreferences(SAFELET_SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
    }

    public long getLong(String key) {
        return preferences.getLong(key, -1);
    }

    public long getLong(String key, long defaultValue) {
        return preferences.getLong(key, defaultValue);
    }

    public void setLong(String key, long value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public int getInt(String key) {
        return preferences.getInt(key, -1);
    }

    public int getInt(String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }

    public void setInt(String key, int value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    public void setBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public String getString(String key) {
        return preferences.getString(key, "");
    }

    public String getString(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }

    public void setString(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public float getFloat(String key) {
        return preferences.getFloat(key, -1);
    }

    public void setFloat(String key, float value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    // Set Follow Me aObjectId
    public void setFollowMeaObjectId(String aObjectId) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("aObjectId", aObjectId);
        editor.apply();
    }

    // Get Follow Me aObjectId
    public String getFollowMeaObjectId() {
        return preferences.getString("aObjectId", "");
    }

    // Set Follow Me aObjectId
    public void setGuardianNetworkSkip(boolean aObjectId) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("GuardianNetworkSkip", aObjectId);
        editor.apply();
    }

    // Get Follow Me aObjectId
    public boolean getGuardianNetworkSkip() {
        return preferences.getBoolean("GuardianNetworkSkip", false);
    }

    public void clear() {
        preferences.edit().clear().apply();
    }

}
