/**
 * Created by Catalin Clabescu on Oct 2, 2014
 * Copyright 2014 XL Team. All rights reserved
 */
package com.safelet.android.views.cells;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.safelet.android.R;

/**
 * Container for rows in {@link}
 *
 * @author catalin
 */
public class CheckInListItem {

    public final TextView contentTextView;
    public final ImageView userImageView;
    public final TextView messageTextView;
    public final TextView locationTextView;

    /**
     * Constructs list checkin cell from view
     */
    public CheckInListItem(View view) {
        contentTextView = (TextView) view.findViewById(R.id.cell_notifications_list_checkin_content_tv);
        userImageView = (ImageView) view.findViewById(R.id.cell_notifications_list_checkin_userimage_iv);
        messageTextView = (TextView) view.findViewById(R.id.cell_notifications_list_checkin_message_tv);
        locationTextView = (TextView) view.findViewById(R.id.cell_notifications_list_checkin_location_tv);
    }
}
