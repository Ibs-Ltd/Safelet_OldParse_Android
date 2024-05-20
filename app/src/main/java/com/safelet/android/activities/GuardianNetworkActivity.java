package com.safelet.android.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.Toolbar;

import com.safelet.android.R;
import com.safelet.android.activities.base.BaseActivity;
import com.safelet.android.fragments.BaseFragment;
import com.safelet.android.fragments.SafeletCommunityJoinedFragment;
import com.safelet.android.fragments.SafeletCommunityNotJoinedFragment;
import com.safelet.android.global.PreferencesManager;
import com.safelet.android.interactors.UserManager;

public class GuardianNetworkActivity extends BaseActivity {

    private final UserManager userManager = UserManager.instance();
    private boolean isGuardianNetworkSkip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);

        Toolbar toolbar = findViewById(R.id.safelet_toolbar);
        setSupportActionBar(toolbar);

        isGuardianNetworkSkip = PreferencesManager.instance(mContext).getGuardianNetworkSkip();
        if (isGuardianNetworkSkip) {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        if (savedInstanceState == null) {
            checkCommunityMember();
        }
    }

    public void checkCommunityMember() {
        BaseFragment fragment;
        if (userManager.getUserModel().isCommunityMember()) {
            fragment = new SafeletCommunityJoinedFragment();
//                ((HomeBaseActivity) mContext).addFragmentToRoot(new SafeletCommunityJoinedFragment());
        } else {
            fragment = new SafeletCommunityNotJoinedFragment();
//                ((HomeBaseActivity) mContext).addFragmentToRoot(new SafeletCommunityNotJoinedFragment());
        }
        setTitle(fragment.getTitleResId());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    public void addFragmentToBackStack(BaseFragment fragment) {
        setTitle(fragment.getTitleResId());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
