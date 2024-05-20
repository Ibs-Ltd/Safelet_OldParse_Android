package com.safelet.android.models.event;

import android.os.Parcel;
import android.os.Parcelable;

import com.safelet.android.R;

import java.util.ArrayList;
import java.util.List;

public class NewNotificationEvent implements Parcelable {

    private String objectId;
    private List<String> messageArgs;
    private NotificationType notificationType;

    public NewNotificationEvent(NotificationType notificationType, List<String> messageArgs, String objectId) {
        this.notificationType = notificationType;
        this.messageArgs = messageArgs;
        this.objectId = objectId;
    }

    protected NewNotificationEvent(Parcel in) {
        objectId = in.readString();
        messageArgs = new ArrayList<>();
        in.readList(messageArgs, List.class.getClassLoader());
        notificationType = NotificationType.values()[in.readInt()];
    }

    public List<String> getMessageArgs() {
        return messageArgs;
    }

    public void setMessageArgs(List<String> messageArgs) {
        this.messageArgs = messageArgs;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(objectId);
        dest.writeList(messageArgs);
        dest.writeInt(notificationType.ordinal());
    }

    public static final Creator<NewNotificationEvent> CREATOR = new Creator<NewNotificationEvent>() {
        @Override
        public NewNotificationEvent createFromParcel(Parcel in) {
            return new NewNotificationEvent(in);
        }

        @Override
        public NewNotificationEvent[] newArray(int size) {
            return new NewNotificationEvent[size];
        }
    };

    public enum NotificationType {
        ALARM_FROM_SAFELET(0),
        DISPATCH_ALARM(R.string.dispatch_alarm_message),
        STOP_ALARM(R.string.stop_alarm_message),
        STOP_ALARM_REASON_TEST(R.string.stop_alarm_reason_message),
        STOP_ALARM_REASON_ACCIDENTAL(R.string.stop_alarm_reason_message),
        STOP_ALARM_REASON_NOT_NEEDED(R.string.stop_alarm_reason_message),
        STOP_ALARM_REASON_OTHER(R.string.stop_alarm_reason_message),
        JOIN_ALARM(R.string.join_alarm_message),
        CALLED_EMERGENCY_NOTIFICATION(R.string.call_emergency_message),
        CHECK_IN_SHORT(R.string.check_in_short_message),
        CHECK_IN_LONG(R.string.check_in_long_message),
        SEND_INVITATION(R.string.send_invitation_message),
        ACCEPT_INVITATION(R.string.accept_invitation_message),
        REJECT_INVITATION(R.string.reject_invitation_message),
        CANCEL_INVITATION(R.string.cancel_invitation_message),
        REMOVE_CONNECTION_GUARDIAN(R.string.remove_connection_guardian_message),
        REMOVE_CONNECTION_GUARDED(R.string.remove_connection_guarded_message),
        START_FOLLOW_ME(R.string.str_start_follow_me_message),
        STOP_FOLLOW_ME(R.string.str_stop_follow_me_message),
        STOP_FOLLOW_USER(R.string.str_stop_follow_user_message),
        POLICY_NOTIFICATION(R.string.str_policy_notification_msg);

        private int messageResId;

        NotificationType(int messageResId) {
            this.messageResId = messageResId;
        }

        public int getMessageResId() {
            return messageResId;
        }

        public static NotificationType fromId(String alertType) {
            for (NotificationType notificationType : values()) {
                if (notificationType.toString().equals(alertType)) {
                    return notificationType;
                }
            }
            return null;
        }
    }
}
