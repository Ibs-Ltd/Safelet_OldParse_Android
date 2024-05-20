package com.safelet.android.views.cells;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.safelet.android.R;

/**
 * Container for rows in {@link fragments.EventsListFragment}
 *
 * @author catalin
 */
public class InvitationListItem {

    public final TextView contentTextView;
    public final ImageView userImageView;
    public final TextView acceptButton;
    public final TextView declineButton;

    /**
     * Constructs invite cell from view
     */
    public InvitationListItem(View view) {
        contentTextView = (TextView) view.findViewById(R.id.cell_notifications_list_invite_content_tv);
        userImageView = (ImageView) view.findViewById(R.id.cell_notifications_list_invite_userimage_iv);
        acceptButton = (TextView) view.findViewById(R.id.cell_notifications_list_invite_accept_tv);
        declineButton = (TextView) view.findViewById(R.id.cell_notifications_list_invite_decline_tv);
    }
}
