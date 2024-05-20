package com.safelet.android.views.cells;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.safelet.android.R;

/**
 * Container for rows in {@link}
 *
 * @author catalin
 */
public class NotificationsActiveAlarmListItem {

    public final TextView contentTextView;
    public final ImageView userImageView;
    public final Button seeDetailsButton;
    public final Button ignoreButton;

    /**
     * Constructs top alert cell from view
     */
    public NotificationsActiveAlarmListItem(View view) {
        contentTextView = (TextView) view.findViewById(R.id.notifications_top_alert_content_tv);
        userImageView = (ImageView) view.findViewById(R.id.notifications_top_alert_profile_picture_iv);
        seeDetailsButton = (Button) view.findViewById(R.id.notifications_top_alert_see_details_tv);
        ignoreButton = (Button) view.findViewById(R.id.notifications_top_alert_ignore);
    }
}
