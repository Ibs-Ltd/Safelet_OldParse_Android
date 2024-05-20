package com.safelet.android.global;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.multidex.MultiDex;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.crashlytics.internal.common.CrashlyticsCore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.SaveCallback;
import com.parse.fcm.ParseFCM;
import com.polidea.rxandroidble.RxBleClient;
import com.safelet.android.BuildConfig;
import com.safelet.android.R;
import com.safelet.android.activities.base.BaseActivity;
import com.safelet.android.interactors.PhoneContactsManager;
import com.safelet.android.interactors.UserManager;
import com.safelet.android.models.Alarm;
import com.safelet.android.models.AlarmRecordingChunk;
import com.safelet.android.models.CheckIn;
import com.safelet.android.models.Firmware;
import com.safelet.android.models.GuardianInvitation;
import com.safelet.android.models.NonUserInvitation;
import com.safelet.android.models.StopReason;
import com.safelet.android.models.UserModel;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import timber.log.Timber;

/**
 * <b>Main application class</b><br/><br/>
 * Initializes parse, Calligraphy and Crashlytics
 * <br/><br/>
 * Provides context in a static field that could be used from anywhere in the application
 */
public class ApplicationSafelet extends Application {

    /**
     * Application context
     */
    private static Context sContext;

    private static RxBleClient rxBleClient;

    public static Context getContext() {
        return sContext;
    }

    public static RxBleClient getBleClient() {
        if (rxBleClient == null) {
            rxBleClient = RxBleClient.create(getContext());
        }
        return rxBleClient;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        initFonts();
        initParse();

        PhoneContactsManager phoneContactsManager = PhoneContactsManager.instance();
        UserManager userManager = UserManager.instance();

        if (userManager.isUserLoggedIn() && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED) {
            phoneContactsManager.initCountryCode(this, userManager.getUserModel().getCountryPrefixCode());
            phoneContactsManager.readPhoneContactsAsync(getContentResolver());
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private void initFonts() {


        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/SourceSansPro-Regular.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());
    }

    private void initParse() {
        ParseObject.registerSubclass(UserModel.class);
        ParseObject.registerSubclass(GuardianInvitation.class);
        ParseObject.registerSubclass(Alarm.class);
        ParseObject.registerSubclass(StopReason.class);
        ParseObject.registerSubclass(CheckIn.class);
        ParseObject.registerSubclass(AlarmRecordingChunk.class);
        ParseObject.registerSubclass(NonUserInvitation.class);
        ParseObject.registerSubclass(Firmware.class);
//        ParseObject.registerSubclass(FollowBean.class);

        /*Parse.Configuration.Builder parseConfigurationBuilder = new Parse.Configuration.Builder(this);
        parseConfigurationBuilder.applicationId(getString(R.string.parse_app_id))
                .clientKey(getString(R.string.parse_client_key))
                .server(getString(R.string.parse_server));
        Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);

        Parse.initialize(parseConfigurationBuilder.build());
        ParseInstallation.getCurrentInstallation().saveInBackground();*/

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.parse_app_id))
                .clientKey(getString(R.string.parse_client_key))
                .server(getString(R.string.parse_server))
                .build());

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    return;
                }
                String token = task.getResult();
                ParseFCM.register(token);
            }
        });
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("GCMSenderId", "32926216083");
        installation.saveInBackground();

        ParsePush.subscribeInBackground("", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Timber.tag(BaseActivity.TAG).i("A: successfully subscribed to the broadcast channel.");
                } else {
                    Timber.tag(BaseActivity.TAG).i(e, "B: failed to subscribe for push");
                }
            }
        });
    }
}
