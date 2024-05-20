package com.safelet.android.views.cells;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.safelet.android.R;

public class NotificationsDisabledAlarmListItem {

    public final TextView titleTextView;
    public final TextView subtitleTextView;
    public final TextView stopReasonTextView;
    public final ImageView userImageView;
    public final TextView playButton;

    /**
     * Constructs top alert cell from view
     */
    public NotificationsDisabledAlarmListItem(View view) {
        titleTextView = (TextView) view.findViewById(R.id.alarm_event_title);
        subtitleTextView = (TextView) view.findViewById(R.id.alarm_event_subtitle);
        stopReasonTextView = (TextView) view.findViewById(R.id.alarm_event_stop_reason);
        userImageView = (ImageView) view.findViewById(R.id.alarm_event_image);
        playButton = (TextView) view.findViewById(R.id.alarm_event_play_button);
    }
}
