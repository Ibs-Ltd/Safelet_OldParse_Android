package com.safelet.android.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.Spanned;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.parse.ParseObject;
import com.safelet.android.R;
import com.safelet.android.interactors.DeviceInformationsManager;
import com.safelet.android.interactors.EventsManager;
import com.safelet.android.interactors.UserManager;
import com.safelet.android.interactors.callbacks.OnResponseListCallback;
import com.safelet.android.utils.Error;
import com.safelet.android.utils.ParsePushUtil;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

public final class SplashActivity extends Activity {

    private static final int GO_TO_LOGIN_WITH_DELAY = 2000;
    private static final int GO_TO_HOME_WITH_DELAY = 1000;
//    private ProgressBar progressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
//        progressBar = findViewById(R.id.progressBar1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        goToNextScreen();
    }

    private void goToNextScreen() {
        // User validation handling
        if (UserManager.instance().isUserLoggedIn()) {
            DeviceInformationsManager.instance().uploadPhoneDetails();
            ParsePushUtil.subscribeInBackground(UserManager.instance().getUserId());

            FirebaseCrashlytics.getInstance().setUserId(UserManager.instance().getUserId());
            FirebaseCrashlytics.getInstance().setCustomKey("setUserEmail", UserManager.instance().getUserEmail());
            FirebaseCrashlytics.getInstance().setCustomKey("setUserName", UserManager.instance().getUserName());

            getNotificationsIfIsNeeded();
        } else {
//            progressBar.setVisibility(View.GONE);
            new Handler(Looper.myLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    // No previous user data found, starts action chooser activity
                    startActivity(new Intent(SplashActivity.this, LoginCreateActivity.class));
                    finish();
                }
            }, GO_TO_LOGIN_WITH_DELAY);


        }
    }

    private void openPrivacy() {
//        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_url))));
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_url1))));
    }

    private void getNotificationsIfIsNeeded() {
        if (EventsManager.instance().getEvents().size() == 0) {
            EventsManager.instance().getEventsForUser(UserManager.instance().getUserId(), new GetNotificationsListener(this));
        } else {
            goToHomeScreen();
        }
    }

    private void goToHomeScreen() {
        new Handler(Looper.myLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                finish();
            }
        }, GO_TO_HOME_WITH_DELAY);
    }

    private static class GetNotificationsListener implements OnResponseListCallback {
        private WeakReference<SplashActivity> weakReference;

        GetNotificationsListener(SplashActivity splashActivity) {
            weakReference = new WeakReference<>(splashActivity);
        }

        @Override
        public void onFailed(Error error) {
            goToNextState();
        }

        @Override
        public void onSuccess(Map<String, List<ParseObject>> objects) {
            goToNextState();
        }

        private void goToNextState() {
            SplashActivity activity = weakReference.get();
            if (activity != null) {
                activity.goToHomeScreen();
            }
        }
    }
}
