package com.safelet.android.views.cells;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.safelet.android.R;

public class InvitationDisabledListItem {

    public final TextView titleTextView;
    public final TextView subtitleTextView;
    public final ImageView userImageView;

    /**
     * Constructs invite cell from view
     */
    public InvitationDisabledListItem(View view) {
        titleTextView = (TextView) view.findViewById(R.id.invite_title);
        userImageView = (ImageView) view.findViewById(R.id.invite_user_image_view);
        subtitleTextView = (TextView) view.findViewById(R.id.invite_subtitle);
    }
}
