package com.safelet.android.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.Toolbar;

import com.safelet.android.R;
import com.safelet.android.activities.base.BaseActivity;
import com.safelet.android.fragments.BaseFragment;
import com.safelet.android.fragments.InviteGuardiansFragment;

/**
 * Activity to invite guardians
 * <p/>
 * Created by alin on 14.10.2015.
 */
public class InviteGuardiansActivity extends BaseActivity {

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
            BaseFragment fragment = new InviteGuardiansFragment();
            setTitle(fragment.getTitleResId());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
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
