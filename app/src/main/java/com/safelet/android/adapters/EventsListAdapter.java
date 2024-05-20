package com.safelet.android.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.parse.ParseObject;
import com.safelet.android.R;
import com.safelet.android.global.Utils;
import com.safelet.android.interactors.callbacks.EventsListAdapterCallback;
import com.safelet.android.models.Alarm;
import com.safelet.android.models.CheckIn;
import com.safelet.android.models.GuardianInvitation;
import com.safelet.android.models.StopReason;
import com.safelet.android.models.enums.UserRelationStatus;
import com.safelet.android.views.cells.CheckInListItem;
import com.safelet.android.views.cells.InvitationDisabledListItem;
import com.safelet.android.views.cells.InvitationListItem;
import com.safelet.android.views.cells.NotificationsActiveAlarmListItem;
import com.safelet.android.views.cells.NotificationsDisabledAlarmListItem;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * Adapter for list in events/notification screen {@link}
 */
public final class EventsListAdapter extends BaseAdapter {

    private static final DateFormat ALARM_DATE_FORMAT = SimpleDateFormat.getDateTimeInstance();

    private static final int ALARM_NOTIFICATION_VIEW_TYPE = 0;
    private static final int INVITATION_NOTIFICATION_VIEW_TYPE = 1;
    private static final int CHECKIN_NOTIFICATION_VIEW_TYPE = 2;

    private Context context;
    private EventsListAdapterCallback callback;
    private List<ParseObject> notificationsList = new ArrayList<>();

    private String playingAlarmId;

    public EventsListAdapter(Context context, EventsListAdapterCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    @Override
    public int getCount() {
        return notificationsList != null ? notificationsList.size() : 0;
    }

    @Override
    public ParseObject getItem(int position) {
        return notificationsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ParseObject model = notificationsList.get(position);
        int itemType = this.getItemViewType(position);
        switch (itemType) {
            case ALARM_NOTIFICATION_VIEW_TYPE:
                return getAlarmView((Alarm) model, convertView, parent);
            case INVITATION_NOTIFICATION_VIEW_TYPE:
                return getInviteView((GuardianInvitation) model, convertView, parent);
            default:
                return getCheckView((CheckIn) model, convertView, parent);
        }
    }

    @Override
    public int getItemViewType(int position) {
        ParseObject model = notificationsList.get(position);
        if (model instanceof Alarm) {
            return ALARM_NOTIFICATION_VIEW_TYPE;
        } else if (model instanceof CheckIn) {
            return CHECKIN_NOTIFICATION_VIEW_TYPE;
        } else {
            return INVITATION_NOTIFICATION_VIEW_TYPE;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public boolean isEnabled(int position) {
        ParseObject model = notificationsList.get(position);
        return !(model instanceof Alarm) && model instanceof CheckIn;
    }

    public void setData(List<ParseObject> notifications) {
        this.notificationsList = notifications;
        notifyDataSetChanged();
    }

    public void setPlayingAlarmId(String alarmId) {
        playingAlarmId = alarmId;
    }

    private View getAlarmView(Alarm alarmModel, View convertView, ViewGroup parent) {
        if (convertView == null ||
                (convertView.getTag() instanceof NotificationsActiveAlarmListItem && !alarmModel.isAlarmActive()) ||
                (convertView.getTag() instanceof NotificationsDisabledAlarmListItem && alarmModel.isAlarmActive())) {
            if (alarmModel.isAlarmActive()) {
                convertView = LayoutInflater.from(context).inflate(R.layout.cell_notification_top_alert, parent, false);
                NotificationsActiveAlarmListItem alertCell = new NotificationsActiveAlarmListItem(convertView);
                convertView.setTag(alertCell);
                bindActiveAlarmView(alertCell, alarmModel);
            } else {
                convertView = LayoutInflater.from(context).inflate(R.layout.cell_notification_top_alert_disabled, parent, false);
                NotificationsDisabledAlarmListItem alertCell = new NotificationsDisabledAlarmListItem(convertView);
                convertView.setTag(alertCell);
                bindDisabledAlarmView(alertCell, alarmModel);
            }
        } else {
            if (convertView.getTag() instanceof NotificationsActiveAlarmListItem) {
                NotificationsActiveAlarmListItem alertCell = (NotificationsActiveAlarmListItem) convertView.getTag();
                bindActiveAlarmView(alertCell, alarmModel);
            } else {
                NotificationsDisabledAlarmListItem alertCell = (NotificationsDisabledAlarmListItem) convertView.getTag();
                bindDisabledAlarmView(alertCell, alarmModel);
            }
        }
        return convertView;
    }

    private void bindActiveAlarmView(NotificationsActiveAlarmListItem alertCell, final Alarm alarmModel) {
        String userPicUrl = alarmModel.getUserImageUrl();
        if (!userPicUrl.isEmpty()) {
            Picasso.get().load(userPicUrl).placeholder(R.drawable.generic_icon)
                    .into(alertCell.userImageView);
        } else {
            alertCell.userImageView.setImageResource(R.drawable.generic_icon);
        }

        alertCell.contentTextView.setText(String.format(context.getString(R.string.notification_xneedsyourhelp),
                alarmModel.getUser().getName()));

        alertCell.seeDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onAlarmDetails(alarmModel);
            }
        });
        alertCell.ignoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onIgnoreAlarm(alarmModel);
            }
        });
    }

    private void bindDisabledAlarmView(NotificationsDisabledAlarmListItem alertCell, final Alarm alarmModel) {
        String userPicUrl = alarmModel.getUserImageUrl();
        if (!userPicUrl.isEmpty()) {
            Picasso.get().load(userPicUrl).placeholder(R.drawable.generic_icon)
                    .into(alertCell.userImageView);
        } else {
            alertCell.userImageView.setImageResource(R.drawable.generic_icon);
        }

        alertCell.titleTextView.setText(String.format(context.getString(R.string.notification_xhasdispatcheddalert),
                alarmModel.getUser().getName()));
        if (alarmModel.getRecordingChunksCount() == 0) {
            alertCell.playButton.setText(R.string.notification_cell_play_disabled);
            alertCell.playButton.setEnabled(false);
        } else {
            if (alarmModel.getObjectId().equals(playingAlarmId)) {
                alertCell.playButton.setText(R.string.notification_cell_stop);
            } else {
                alertCell.playButton.setText(R.string.notification_cell_play);
            }
            alertCell.playButton.setEnabled(true);
        }
        alertCell.subtitleTextView.setText(ALARM_DATE_FORMAT.format(alarmModel.getCreatedAt()));
        alertCell.subtitleTextView.setTypeface(null, Typeface.ITALIC);

        StopReason stopReason = alarmModel.getStopReason();

        if (stopReason != null) {
            String reasonDescription;
            switch (stopReason.getReason()) {
                case TEST_ALARM:
                    reasonDescription = context.getString(R.string.disable_reason_test);
                    break;
                case ACCIDENTAL_ALARM:
                    reasonDescription = context.getString(R.string.disable_reason_accidental);
                    break;
                case ALARM_NOT_NEEDED:
                    reasonDescription = context.getString(R.string.disable_reason_not_needed);
                    break;
                default:
                    reasonDescription = stopReason.getReasonDescription();
                    break;
            }
            alertCell.stopReasonTextView.setText(context.getString(R.string.disable_reason, reasonDescription));
        } else {
            alertCell.stopReasonTextView.setText(null);
        }

        alertCell.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alarmModel.getObjectId().equals(playingAlarmId)) {
                    callback.onStopButtonClicked();
                } else {
                    callback.onPlayButtonClicked(alarmModel);
                }
            }
        });
    }

    private View getInviteView(final GuardianInvitation invitationModel, View convertView, ViewGroup parent) {
        UserRelationStatus invitationStatus = invitationModel.getRelationStatus();
        if (convertView == null ||
                (invitationStatus.equals(UserRelationStatus.PENDING) && (convertView.getTag() instanceof InvitationDisabledListItem)) ||
                (!invitationStatus.equals(UserRelationStatus.PENDING) && (convertView.getTag() instanceof InvitationListItem))) {
            if (invitationStatus.equals(UserRelationStatus.PENDING)) {
                convertView = LayoutInflater.from(context).inflate(R.layout.cell_notifications_list_invite, parent, false);
                InvitationListItem inviteListCell = new InvitationListItem(convertView);
                convertView.setTag(inviteListCell);

                bindInvitationPendingView(inviteListCell, invitationModel);
            } else {
                convertView = LayoutInflater.from(context).inflate(R.layout.cell_notifications_list_invite_disabled, parent, false);
                InvitationDisabledListItem inviteListCell = new InvitationDisabledListItem(convertView);
                convertView.setTag(inviteListCell);

                bindInvitationDisabledView(inviteListCell, invitationModel);
            }
        } else {
            if (convertView.getTag() instanceof InvitationListItem) {
                InvitationListItem inviteListCell = (InvitationListItem) convertView.getTag();
                bindInvitationPendingView(inviteListCell, invitationModel);
            } else {
                InvitationDisabledListItem inviteListCell = new InvitationDisabledListItem(convertView);
                bindInvitationDisabledView(inviteListCell, invitationModel);
            }
        }

        return convertView;
    }

    private void bindInvitationPendingView(InvitationListItem inviteListCell, final GuardianInvitation invitationModel) {
        inviteListCell.contentTextView.setText(String.format(context.getString(R.string.notification_xhasrequestedyouasguardian),
                invitationModel.getFromUser().getName()));
        String fromUserPicUrl = invitationModel.getFromUser().getImageUrl();
        if (!fromUserPicUrl.isEmpty()) {
            Picasso.get().load(fromUserPicUrl)
                    .placeholder(R.drawable.generic_icon)
                    .into(inviteListCell.userImageView);
        } else {
            inviteListCell.userImageView.setImageResource(R.drawable.generic_icon);
        }

        inviteListCell.acceptButton.setEnabled(true);
        inviteListCell.acceptButton.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(final View acceptView) {
                        acceptView.setEnabled(false);
                        callback.onAccept(invitationModel);
                    }
                });
        inviteListCell.declineButton.setEnabled(true);
        inviteListCell.declineButton.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(final View declineView) {
                        declineView.setEnabled(false);
                        callback.onDecline(invitationModel);
                    }
                });
    }

    private void bindInvitationDisabledView(InvitationDisabledListItem inviteListCell, final GuardianInvitation invitationModel) {
        String mName = "";
        if (invitationModel.getFromUser().getName() != null)
            mName = invitationModel.getFromUser().getName();
        inviteListCell.titleTextView.setText(String.format(context.getString(R.string.notification_xhasrequestedyouasguardian), mName));
        String fromUserPicUrl = invitationModel.getFromUser().getImageUrl();
        if (!fromUserPicUrl.isEmpty()) {
            Picasso.get().load(fromUserPicUrl)
                    .placeholder(R.drawable.generic_icon)
                    .into(inviteListCell.userImageView);
        } else {
            inviteListCell.userImageView.setImageResource(R.drawable.generic_icon);
        }
        UserRelationStatus invitationStatus = invitationModel.getRelationStatus();
        switch (invitationStatus) {
            case NONE:
                inviteListCell.subtitleTextView.setText(R.string.notification_cell_canceled);
                break;
            case ACCEPTED:
                inviteListCell.subtitleTextView.setText(R.string.notification_cell_accepted);
                break;
            case REJECTED:
                inviteListCell.subtitleTextView.setText(R.string.notification_cell_rejected);
                break;
        }
        inviteListCell.subtitleTextView.setTypeface(null, Typeface.ITALIC);
    }

    private View getCheckView(final CheckIn model, View convertView, ViewGroup parent) {
        CheckInListItem checkInListCell;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.cell_notifications_list_checkin, parent, false);
            checkInListCell = new CheckInListItem(convertView);
            convertView.setTag(checkInListCell);
        } else {
            checkInListCell = (CheckInListItem) convertView.getTag();
        }
        convertView.setBackgroundColor(Color.WHITE);

        checkInListCell.contentTextView.setText(String.format(context
                .getString(R.string.notification_xhascheckedin), model.getUserName())
                + Utils.getTimeAgo(model.getCreatedAt().getTime(), context.getString(R.string.notification_justnow)));
        String userPicUrl = model.getUserImageUrl();
        if (!userPicUrl.isEmpty()) {
            Picasso.get().load(userPicUrl)
                    .placeholder(R.drawable.generic_icon).into(checkInListCell.userImageView);
        } else {
            checkInListCell.userImageView.setImageResource(R.drawable.generic_icon);
        }
        checkInListCell.messageTextView.setText(model.getCheckInMessage());
        checkInListCell.locationTextView.setText(String.format(context
                .getString(R.string.notification_location), model.getLocationName()));
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onCheckIn(model);
            }
        });
        return convertView;
    }
}
