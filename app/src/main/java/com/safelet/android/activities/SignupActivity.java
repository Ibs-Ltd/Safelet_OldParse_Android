package com.safelet.android.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.safelet.android.R;
import com.safelet.android.activities.base.BaseActivity;
import com.safelet.android.fragments.BaseFragment;
import com.safelet.android.fragments.SignupFirstFragment;

/**
 * Sign up user activity
 * <p/>
 * Created by Alin on 10/18/2015.
 */
public class SignupActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);
        Toolbar toolbar = (Toolbar) findViewById(R.id.safelet_toolbar);
        toolbar.setVisibility(View.GONE);
        if (savedInstanceState == null) {
            BaseFragment fragment = new SignupFirstFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    @Override
    public void addFragmentToBackStack(BaseFragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }



}
