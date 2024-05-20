package com.safelet.android.global;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.inputmethod.InputMethodManager;

import com.safelet.android.R;
import com.safelet.android.activities.base.BaseActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import timber.log.Timber;

public final class Utils {
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    private Utils() {
        throw new UnsupportedOperationException();
    }

    public static boolean validateEmailWithString(String email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static String getTimeAgo(long time, String ifNow) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000L;
        }
        Calendar nowCalendar = Calendar.getInstance();
        nowCalendar.setTimeZone(TimeZone.getTimeZone("gmt"));
        long now = nowCalendar.getTimeInMillis();
        if (time > now || time <= 0) {
            time = now;
        }
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return ifNow;
        } else if (diff < 2 * MINUTE_MILLIS) {
            return ApplicationSafelet.getContext().getString(R.string.calendar_aminuteago);
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + ApplicationSafelet.getContext().getString(R.string.calendar_minutesago);
        } else if (diff < 90 * MINUTE_MILLIS) {
            return ApplicationSafelet.getContext().getString(R.string.calendar_ahourago);
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + ApplicationSafelet.getContext().getString(R.string.calendar_hoursago);
        } else if (diff < 48 * HOUR_MILLIS) {
            return ApplicationSafelet.getContext().getString(R.string.calendar_yesterday);
        } else {
            return diff / DAY_MILLIS + ApplicationSafelet.getContext().getString(R.string.calendar_daysago);
        }
    }

    /**
     * Hides keyboard from screen / Activity
     *
     * @param activity Activity that displays the keyboard
     */
    public static void hideKeyboard(Activity activity) {
        try {
            InputMethodManager inputManager = (InputMethodManager) activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (activity.getCurrentFocus() != null) {
                inputManager.hideSoftInputFromWindow(activity.getCurrentFocus()
                        .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception e) {
            Log.e(Utils.class.getSimpleName(), "Error hiding keyboard");
        }
    }

    /**
     * Converts input stream to string
     *
     * @param in Input stream to be converted
     * @return String of the input stream
     */
    public static String inputStream2String(InputStream in) {
        StringBuilder out = new StringBuilder();
        byte[] b = new byte[2048];
        try {
            for (int i; (i = in.read(b)) != -1; ) {
                out.append(new String(b, 0, i));
            }
        } catch (IOException e) {
            Log.e(Utils.class.getSimpleName(), "inputStream2String IOException");
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                Log.e(Utils.class.getSimpleName(), "inputStream2String IOException");
            }
        }
        return out.toString();
    }

    /**
     * Checks if application has network connectivity
     *
     * @return Network connectivity status
     */
    public static boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) ApplicationSafelet.getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connMgr.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    /**
     * Checks if GPS is enabled and displays an message if
     * is inactive that enables the user to activate it from device settings
     *
     * @param ctx Context in which is checking, please pass activity
     */
    public static void isGPSEnabled(final Context ctx) {
        if (!isGPSEnabledFromSettings(ctx) && (ctx instanceof Activity) && !((Activity) ctx).isFinishing()) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(
                    ctx);
            builder.setTitle(ctx.getString(R.string.utils_gps_enabling_title))
                    .setMessage(ctx.getString(R.string.utils_gps_enabling_message))
                    .setCancelable(false)
                    .setPositiveButton(ctx.getString(R.string.utils_gps_enabling_yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        final DialogInterface dialog,
                                        final int id) {
                                    ctx.startActivity(
                                            new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                }
                            })
                    .setNegativeButton(ctx.getString(R.string.utils_gps_enabling_no),
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        final DialogInterface dialog,
                                        final int id) {
                                    dialog.cancel();
                                }
                            });
            final AlertDialog alert = builder.create();
            alert.show();
        }
    }

    /**
     * Checks android system settings for verify GPS status
     *
     * @param ctx Context in which is checking
     * @return status of gps
     */
    private static boolean isGPSEnabledFromSettings(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //Only for kitkat and newer versions
            int provider = -1;
            try {
                provider = Settings.Secure.getInt(ctx.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                //No need to handle this
            }
            return provider == Settings.Secure.LOCATION_MODE_HIGH_ACCURACY;
        } else {
            @SuppressWarnings("deprecation") String allowedLocationProviders =
                    Settings.System.getString(ctx.getContentResolver(),
                            Settings.System.LOCATION_PROVIDERS_ALLOWED);

            if (allowedLocationProviders == null) {
                allowedLocationProviders = "";
            }
            return allowedLocationProviders.contains(LocationManager.GPS_PROVIDER);
        }
    }

    /**
     * Navigates to network settings in Android system
     *
     * @param context Context in which is called
     */
    public static void showNetworkSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        context.startActivity(intent);
    }

    public static int pixelsFromDp(int dp) {
        Resources r = ApplicationSafelet.getContext().getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    private static int textWidth(String str) {
        return str.length();
    }

    /**
     * Shortens the text
     *
     * @param text Text to be shortened
     * @param max  Maximum allowed characters
     * @return Shortened text with length of max
     */
    public static String ellipsize(String text, int max) {
        if (textWidth(text) <= max) {
            return text;
        }
        text = text.substring(0, max);
        return text + "...";
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    public static byte[] convertMacAddress(String macAddress) {
        String[] macAddressParts = macAddress.split(":");

        // convert hex string to byte values
        byte[] macAddressBytes = new byte[6];
        for (int i = 0; i < 6; i++) {
            Integer hex = Integer.parseInt(macAddressParts[i], 16);
            macAddressBytes[i] = hex.byteValue();
        }
        return macAddressBytes;
    }

    public static String convertMacAddress(byte[] macAddress) {
        StringBuilder macAddressString = new StringBuilder();
        for (int i = 0; i < macAddress.length; i++) {
            macAddressString.append(String.format("%02X", macAddress[i]));
            if (i != macAddress.length - 1) {
                macAddressString.append(":");
            }
        }
        return macAddressString.toString();
    }

    public static String createNotificationChannel(Context context, String channelName) {
        return createNotificationChannel(context, channelName, false);
    }

    public static String createNotificationChannel(Context context, String channelName, boolean highImportance) {
        Timber.tag(BaseActivity.TAG).d("".concat("createNotificationChannel"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = String.valueOf(channelName.hashCode());
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager.getNotificationChannel(channelId) == null) {
                notificationManager.createNotificationChannel(new NotificationChannel(channelId, channelName,
                        highImportance ? NotificationManager.IMPORTANCE_HIGH : NotificationManager.IMPORTANCE_DEFAULT));

//                notificationManager.createNotificationChannel(new NotificationChannel(channelId, channelName,
//                        highImportance ? NotificationManager.IMPORTANCE_DEFAULT : NotificationManager.IMPORTANCE_DEFAULT));
            }
            return channelId;
        } else {
            return channelName;
        }
    }

    public static String createNotificationChannelNew(Context context, String channelName, boolean highImportance) {
        Timber.tag(BaseActivity.TAG).d("".concat("createNotificationChannel"));
        Uri defaultSoundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + context.getPackageName() + "/raw/mysound");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = String.valueOf(channelName.hashCode());
            NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager.getNotificationChannel(channelId) == null) {
//                notificationManager.createNotificationChannel(new NotificationChannel(channelId, channelName,
//                        highImportance ? NotificationManager.IMPORTANCE_DEFAULT : NotificationManager.IMPORTANCE_DEFAULT));

                // New
                NotificationChannel mChannel = new NotificationChannel(channelId
                        , channelName, NotificationManager.IMPORTANCE_HIGH);
//                NotificationChannel mChannel = new NotificationChannel(channelId
//                        , channelName, NotificationManager.IMPORTANCE_DEFAULT);
                mChannel.enableLights(true);
                mChannel.enableVibration(true);
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build();
//                mChannel.setSound(defaultSoundUri, audioAttributes);
                Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                mChannel.setSound(uri, audioAttributes);
                mChannel.setLightColor(Color.GRAY);
                notificationManager.createNotificationChannel(mChannel);
            }
            return channelId;
        } else {
            return channelName;
        }
    }

    public static String getAddressLatLon(Context mContext, double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Timber.tag(BaseActivity.TAG).w(" Location Address ".concat(strReturnedAddress.toString()));
            } else {
                Timber.tag(BaseActivity.TAG).w(" No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Timber.tag(BaseActivity.TAG).w(" Cannot get Address!");
        }
        return strAdd;
    }
}
