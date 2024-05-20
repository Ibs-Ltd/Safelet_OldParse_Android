package com.safelet.android.activities;

import static com.safelet.android.utils.Utility.hideKeyboard;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.Toolbar;

import com.safelet.android.R;
import com.safelet.android.activities.base.BaseActivity;
import com.safelet.android.fragments.BaseFragment;
import com.safelet.android.fragments.ChangePasswordFragment;
import com.safelet.android.fragments.ChangePhoneNumberFragment;
import com.safelet.android.fragments.EditEmailFragment;
import com.safelet.android.fragments.EditNameFragment;

/**
 * Activity used to show the edit user information screens
 * <p/>
 * Created by alin on 13.10.2015.
 */
public class EditUserInformationActivity extends BaseActivity {

    public static final String INFO_TYPE_KEY = "euia.infoType.key";

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
                hideKeyboard(EditUserInformationActivity.this);
                finish();
            }
        });

        if (savedInstanceState == null) {
            UserInfo info = (UserInfo) getIntent().getSerializableExtra(INFO_TYPE_KEY);
            BaseFragment fragment;
            switch (info) {
                case NAME:
                    fragment = new EditNameFragment();
                    break;
                case EMAIL:
                    fragment = new EditEmailFragment();
                    break;
                case PHONE:
                    fragment = new ChangePhoneNumberFragment();
                    break;
                default:
                    fragment = new ChangePasswordFragment();
            }

            setTitle(getString(fragment.getTitleResId()));

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    public enum UserInfo {
        NAME, EMAIL, PHONE, PASSWORD
    }
}
