package com.safelet.android.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.Toolbar;

import com.safelet.android.R;
import com.safelet.android.activities.base.BaseActivity;
import com.safelet.android.fragments.BaseFragment;
import com.safelet.android.fragments.ForgotPasswordFragment;


/**
 * Activity for ForgotPassword Screen
 *
 * @author alin
 */
public class ForgotPasswordActivity extends BaseActivity {
    public static final String INTENT_EXTRA_NAV_KEY = "fpa.forgotPass.key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);

        boolean isForgotPassword = getIntent().getBooleanExtra(INTENT_EXTRA_NAV_KEY, false);
        Toolbar toolbar = (Toolbar) findViewById(R.id.safelet_toolbar);
        if (isForgotPassword) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        } else {
            toolbar.setVisibility(View.GONE);
        }
        if (savedInstanceState == null) {
            BaseFragment fragment = ForgotPasswordFragment.newInstance(isForgotPassword);
            setTitle(getString(fragment.getTitleResId()));
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}