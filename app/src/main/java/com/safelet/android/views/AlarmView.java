package com.safelet.android.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.safelet.android.R;
import com.safelet.android.models.Alarm;

public final class AlarmView extends LinearLayout {
    private TextView descriptionTextView;
    private TextView soundStatusTextView;

    public AlarmView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init(context);
        }
    }

    public AlarmView(Context context) {
        this(context, null);
    }

    private void init(Context context) {
        LayoutInflater lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        lInflater.inflate(R.layout.view_alert, this, true);
        descriptionTextView = findViewById(R.id.viewAlertHelpTextView);
        soundStatusTextView = findViewById(R.id.sound_status);
    }

    public void showAlarm(Alarm alarm, int nrAlarms) {
        if (alarm != null) {
            descriptionTextView.setText(getContext().getString(R.string.alert_pop_ineedhelp));
            setVisibility(View.VISIBLE);
        } else if (nrAlarms > 0) {
            setVisibility(View.VISIBLE);
            descriptionTextView.setText(String.format(getContext()
                    .getString(R.string.alert_pop_peopleneedshelp), nrAlarms));
        } else {
            setVisibility(View.GONE);
        }
    }

    public void setVisibilityNoAnimation(int visibility) {
        super.setVisibility(visibility);
    }

    public void setIsRecording(boolean isRecording) {
        if (isRecording) {
            soundStatusTextView.setVisibility(VISIBLE);
            soundStatusTextView.setText(R.string.recording_text);
        } else {
            soundStatusTextView.setVisibility(GONE);
        }
    }

    public void setNotPlayback() {
        soundStatusTextView.setText("");
    }

    public interface OnClickListener {
        void onClick();
    }
}
