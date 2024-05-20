/**
 * activities LoginCreateActivity.java
 * Safelet
 * Created by Badea Mihai Bogdan on Sep 15, 2014
 * Copyright (c) 2014 XLTeam. All rights reserved.
 */
package com.safelet.android.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.Spanned;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.safelet.android.R;
import com.safelet.android.activities.base.BaseActivity;
import com.safelet.android.global.PreferencesManager;


/**
 * Activity for LoginCreate Screen<br/>
 * Main application navigation entry point after splash screen {@link}
 *
 * @author mihai
 */
public class LoginCreateActivity extends BaseActivity implements OnClickListener {

    private AlertDialog contactDialog;
    LoginCreateActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logincreate);

        activity = this;

        PreferencesManager.instance(this).clear();
        // Programmatically logout
        findViewById(R.id.logincreate_login_btn).setOnClickListener(this);
        findViewById(R.id.logincreate_create_btn).setOnClickListener(this);
        findViewById(R.id.privacy).setOnClickListener(this);
        ImageView wings = findViewById(R.id.background_wings);

        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_wings);
        wings.startAnimation(fadeInAnimation);

//        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS}, 0);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.logincreate_login_btn) {
            onLogin();
        } else if (v.getId() == R.id.logincreate_create_btn) {
            onRegister();
        } else if (v.getId() == R.id.privacy) {
            onPrivacy();
        }
    }

    private void onLogin() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    private void onRegister() {
        startActivity(new Intent(this, SignupActivity.class));
    }

    private void onPrivacy() {
//        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_url))));
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_policy_url))));
    }


    @Override
    protected void onStart() {
        super.onStart();
//        int MyVersion = Build.VERSION.SDK_INT;
//        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
//            if (!checkIfAlreadyhavePermission()) {
//                requestForSpecificPermission();
//            }
//        }
        showExplanationDialog(activity);
    }

    @SuppressLint("SetTextI18n")
    private void showExplanationDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.CustomAlertDialogTheme));

        // Inflate the custom layout for the dialog
        View dialogView = LayoutInflater.from(context).inflate(R.layout.custom_alert_layout, null);
        builder.setView(dialogView);

        // Customize the title and message text (if needed)
        TextView titleTextView = dialogView.findViewById(R.id.dialog_title);
        titleTextView.setText("Why We Need Access to Your Contacts");

        TextView messageTextView = dialogView.findViewById(R.id.dialog_message);

        String messageText = "We value your privacy and want to explain why we need access to your contacts:<br><br>" +
                "<b>1. Contact Information :</b><br><br>" +
                "We upload and store your phone contacts' information securely to provide you with the option to invite your friends and family as guardians in case of emergencies. This includes names, phone numbers, and email addresses.<br><br>" +
                "<b>2. Location Data :</b><br><br>" +
                "Safelet may access your device's location data to share your real-time location with your All guardians when you trigger an SOS Alarm. This is essential for your safety.<br><br>" +
                "<b>3. Emergency Information :</b><br><br>" +
                "To assist first responders, we may collect and transmit vital information about you and emergency contacts, as provided by you.<br><br>" +

                "<b>Note :</b> Users Contact Lists are uploaded to our secure Heroku Server at <b>https://safelet.herokuapp.com</b> while maintaining stringent data privacy and security measures.<br><br>"+

                "This data collection occurs in the following scenarios:<br><br>" +
                "<ul>" +
                "<li>When you choose to add friends from your contact list as guardians.</li>" +
                "<li>When you activate an SOS Alarm, sharing your location and emergency information with your selected guardians.</li>" +
                "</ul>" +

                "<b>4. Your privacy and security :</b><br><br>" +
                "Your privacy and security are our top priorities. We do not share this data with any third parties.<br><br>" +

                "By using Safelet, you agree to the collection, transmission, syncing, and storage of the data mentioned above for the purpose of enhancing your safety.<br><br>" +
                "Please note that your consent is required before Safelet can start collecting or accessing this data. You can provide your consent by continuing to use the app.<br><br>" +
                "If you have any concerns or questions about our data practices, please contact our support team at googleplay@safelet.com";

        Spanned spannedMessage = Html.fromHtml(messageText);
        messageTextView.setText(spannedMessage);

        // Customize buttons and other UI elements as needed

        // Find custom buttons by their IDs
        Button customOKButton = dialogView.findViewById(R.id.positive_button);
        Button customCancelButton = dialogView.findViewById(R.id.negative_button);
        Button customPrivacyPolicyButton = dialogView.findViewById(R.id.neutral_button);

        // Set custom button click actions
        customOKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Customize the action for the OK button
                // For example, dismiss the dialog and start an activity
                contactDialog.dismiss();
                int MyVersion = Build.VERSION.SDK_INT;
                if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
                    if (!checkIfAlreadyhavePermission()) {
                        requestForSpecificPermission();
                    }
                }
            }
        });

        customCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Customize the action for the Cancel button
                // For example, dismiss the dialog
                contactDialog.dismiss();
                finish();
            }
        });

        customPrivacyPolicyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Customize the action for the Privacy Policy button
                // For example, open the privacy policy page
                contactDialog.dismiss();
                onPrivacy();
            }
        });

        // Create and show the AlertDialog
        contactDialog = builder.create();
        contactDialog.show();
    }


    private boolean checkIfAlreadyhavePermission() {
        int result =
                ContextCompat.checkSelfPermission(
                        LoginCreateActivity.this, Manifest.permission.GET_ACCOUNTS);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void requestForSpecificPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                    LoginCreateActivity.this,
                    new String[]{
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.READ_PHONE_NUMBERS,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.CAMERA,
                            Manifest.permission.POST_NOTIFICATIONS
                    },
                    101);
        } else {
            ActivityCompat.requestPermissions(
                    LoginCreateActivity.this,
                    new String[]{
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.READ_PHONE_NUMBERS,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.CAMERA,
                    },
                    101);
        }
    }
}
