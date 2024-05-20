package com.safelet.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.safelet.android.R;
import com.safelet.android.activities.base.BaseActivity;
import com.safelet.android.models.StopReason;

public class DisableAlarmActivity extends BaseActivity {

    private EditText description;
    private Button disableAlarm;

    public static final String KEY_REASON = "reason";
    public static final String KEY_DESCRIPTION = "description";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.disable_alarm);

        description = (EditText) findViewById(R.id.description);
        disableAlarm = (Button) findViewById(R.id.disable_alarm);

        setResult(RESULT_CANCELED);
    }

    public void onReasonClick(View view) {
        if (view.getId() == R.id.reason_other) {
            disableAlarm.setVisibility(View.VISIBLE);
            description.setVisibility(View.VISIBLE);
            description.requestFocus();
        } else {
            Intent intent = new Intent();
            switch (view.getId()) {
                case R.id.reason_test_alarm:
                    intent.putExtra(KEY_REASON, StopReason.Reason.TEST_ALARM);
                    break;
                case R.id.reason_accidental_alarm:
                    intent.putExtra(KEY_REASON, StopReason.Reason.ACCIDENTAL_ALARM);
                    break;
                case R.id.reason_not_needed:
                    intent.putExtra(KEY_REASON, StopReason.Reason.ALARM_NOT_NEEDED);
                    break;
            }
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    public void onDisableAlarmClick(View view) {
        Intent intent = new Intent();
        intent.putExtra(KEY_REASON, StopReason.Reason.OTHER);
        intent.putExtra(KEY_DESCRIPTION, description.getText().toString());
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onCancelClick(View view) {
        finish();
    }
}
