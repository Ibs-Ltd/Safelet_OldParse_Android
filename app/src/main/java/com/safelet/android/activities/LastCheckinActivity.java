/**
 * Created by Badea Mihai Bogdan on Oct 15, 2014
 * Copyright (c) 2014 XLTeam. All rights reserved.
 */
package com.safelet.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.Toolbar;

import com.safelet.android.R;
import com.safelet.android.activities.base.BaseActivity;
import com.safelet.android.fragments.BaseFragment;
import com.safelet.android.fragments.LastCheckinFragment;

/**
 * Activity for LastCheckin Screen<br/>
 * Updates views based on model received from intent
 * <p/>
 * Created by alin on 19.10.2015.
 */
public class LastCheckinActivity extends BaseActivity {

    public static final String KEY_MODEL_CONN = "KEY_MODEL_CONN";
    public static final String KEY_MODEL_CHECKIN = "KEY_MODEL_CHECKIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);

        Toolbar toolbar = findViewById(R.id.safelet_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if (savedInstanceState == null) {
            BaseFragment fragment;
            Intent intent = getIntent();
            if (intent.getExtras().containsKey(KEY_MODEL_CONN)) {
                String userObjectId = intent.getStringExtra(KEY_MODEL_CONN);
                fragment = LastCheckinFragment.newInstanceLastChecking(userObjectId);
            } else {
                String checkInId = intent.getStringExtra(KEY_MODEL_CHECKIN);
                fragment = LastCheckinFragment.newInstanceChecking(checkInId);
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        }
    }
}
