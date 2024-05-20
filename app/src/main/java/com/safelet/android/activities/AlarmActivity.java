package com.safelet.android.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.safelet.android.R;
import com.safelet.android.activities.base.BaseActivity;
import com.safelet.android.callback.NavigationDrawerCallbacks;
import com.safelet.android.fragments.AlarmFragment;
import com.safelet.android.interactors.EventsManager;
import com.safelet.android.models.Alarm;
import com.safelet.android.models.enums.NavigationMenu;

public class AlarmActivity extends BaseActivity implements NavigationDrawerCallbacks {

    public static final String ALARM_ID_KEY = "aa.alarmId.key";
    private Alarm alarm;

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
        String alarmKey = getIntent().getStringExtra(ALARM_ID_KEY);
        alarm = EventsManager.instance().getAlarmById(alarmKey);
        if (alarm == null) {
            Toast.makeText(this, R.string.loading_alarm_failed, Toast.LENGTH_LONG).show();
            finish();
        } else if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, AlarmFragment.newInstance(alarmKey))
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (alarm != null && alarm.isAlarmActive()) {
            setTitle(String.format(getString(R.string.alert_pop_needshelp_1), alarm.getUser().getName()));
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(NavigationMenu menuItem) {
        finish();
    }

    @Override
    public void updateMenu() {
        // do nothing
    }
}
