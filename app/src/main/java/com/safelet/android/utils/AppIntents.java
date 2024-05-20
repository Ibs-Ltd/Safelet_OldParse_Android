package com.safelet.android.utils;

import android.content.Context;
import android.content.Intent;

import com.safelet.android.activities.FollowMeActivity;
import com.safelet.android.activities.IAmHereActivity;

public class AppIntents {

    // Follow Me Intent
    public static void intentFollowMe(Context mContext, double Latitude, double Longitude) {
        Intent iIAmHere = new Intent(mContext, FollowMeActivity.class);
        iIAmHere.putExtra("mLatitude", Latitude);
        iIAmHere.putExtra("mLongitude", Longitude);
        mContext.startActivity(iIAmHere);
    }

    // I'm Here Activity
    public static void intentIAmHere(Context mContext, double Latitude, double Longitude) {
        Intent iIAmHere = new Intent(mContext, IAmHereActivity.class);
        iIAmHere.putExtra("mLatitude", Latitude);
        iIAmHere.putExtra("mLongitude", Longitude);
        mContext.startActivity(iIAmHere);
    }


}
