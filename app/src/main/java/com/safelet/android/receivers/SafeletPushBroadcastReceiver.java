package com.safelet.android.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.parse.ParseObject;
import com.parse.ParsePushBroadcastReceiver;
import com.safelet.android.R;
import com.safelet.android.activities.HomeActivity;
import com.safelet.android.activities.base.BaseActivity;
import com.safelet.android.global.Utils;
import com.safelet.android.interactors.EventBusManager;
import com.safelet.android.interactors.EventsManager;
import com.safelet.android.interactors.UserManager;
import com.safelet.android.interactors.callbacks.OnResponseListCallback;
import com.safelet.android.models.GuardianInvitation;
import com.safelet.android.models.enums.UserRelationStatus;
import com.safelet.android.models.event.GuardianJoinedEvent;
import com.safelet.android.models.event.NewNotificationEvent;
import com.safelet.android.utils.Error;
import com.safelet.android.utils.media.AlarmNotificationSoundPlayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class SafeletPushBroadcastReceiver extends ParsePushBroadcastReceiver {

    public static final String NEW_NOTIFICATION_INTENT = "pnm.newNotificationReceived.intent";
    public static final String NOTIFICATION_EXTRA = "pnm.notificationExtra.key";

    private static final int NOTIFICATION_ID = 1;
    private static final String OBJECT_ID_KEY = "objectId";
    private static final String ALERT_KEY = "alert";
    private static final String TYPE_KEY = "loc-key";
    private static final String ARGS_KEY = "loc-args";

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        Timber.d("".concat(BaseActivity.TAG).concat(" NOTIFICATION RECEIVED"));
        String currentUserId;
        if (!UserManager.instance().isUserLoggedIn()) {
            return;
        } else {
            currentUserId = UserManager.instance().getUserId();
        }

        try {
            JSONObject data = new JSONObject(intent.getStringExtra(KEY_PUSH_DATA));
            String objectId = data.getString(OBJECT_ID_KEY);
            Timber.d("".concat(BaseActivity.TAG).concat("objectId ").concat(objectId));
            JSONObject alertJsonObject = data.getJSONObject(ALERT_KEY);
            String alertType = alertJsonObject.getString(TYPE_KEY);
            Timber.d("".concat(BaseActivity.TAG).concat("alertType").concat(alertType));
            JSONArray argsStringJsonArray = alertJsonObject.getJSONArray(ARGS_KEY);
            List<String> argsList = new ArrayList<>();
            for (int i = 0; i < argsStringJsonArray.length(); i++) {
                argsList.add(argsStringJsonArray.getString(i));
                Timber.d("".concat(BaseActivity.TAG).concat("String ARGS: ").concat(argsStringJsonArray.getString(i)));
            }

            NewNotificationEvent.NotificationType notificationType = NewNotificationEvent.NotificationType.fromId(alertType);
            if (notificationType == null) {
                return;
            }
            Timber.d("".concat(BaseActivity.TAG).concat("notificationType: ").concat(String.valueOf(notificationType)));

            EventsManager.instance().getEventsForUser(currentUserId, new GetNotificationsListener(context.getApplicationContext(),
                    new NewNotificationEvent(notificationType, argsList, objectId)));
        } catch (JSONException ignore) {
//            ignore.printStackTrace();
        }
    }

    @Override
    protected void onPushDismiss(Context context, Intent intent) {
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
    }

    private static void handleNotification(Context context, NewNotificationEvent notificationEvent, ParseObject notificationObject) {
        Uri defaultSoundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + context.getPackageName() + "/raw/mysound");
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context
                , Utils.createNotificationChannel(context,
                context.getString(R.string.notification_channel_update), true))
                .setSmallIcon(R.drawable.notification_safelet_message)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(notificationEvent.getNotificationType().getMessageResId(), notificationEvent.getMessageArgs().toArray()))
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//            notificationBuilder.setSound(defaultSoundUri);
            notificationBuilder.setSound(uri);
        }
        Notification notification = notificationBuilder.build();
        notification.contentIntent = getNotificationPendingIntent(context
                , notificationEvent, notification, notificationObject);

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);

        Intent notificationIntent = new Intent(NEW_NOTIFICATION_INTENT);
        notificationIntent.putExtra(NOTIFICATION_EXTRA, notificationEvent);
        LocalBroadcastManager.getInstance(context).sendBroadcast(notificationIntent);
    }

    private static PendingIntent getNotificationPendingIntent(Context context, NewNotificationEvent newNotificationEvent,
                                                              Notification notification, ParseObject notificationObject) {
        Intent eventIntent = new Intent(context, HomeActivity.class);
        eventIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        eventIntent.putExtra(HomeActivity.EXTRA_FROM_NOTIFICATION_KEY, newNotificationEvent);
        PendingIntent eventPendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis()
                , eventIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Intent homeIntent = new Intent(context, HomeActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent homePendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis()
                , homeIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        switch (newNotificationEvent.getNotificationType()) {
            case SEND_INVITATION:
            case ACCEPT_INVITATION:
            case REJECT_INVITATION:
            case CANCEL_INVITATION:
                GuardianInvitation guardianInvitation = (GuardianInvitation) notificationObject;
                if (guardianInvitation != null && guardianInvitation.getRelationStatus().equals(UserRelationStatus.PENDING)) {
                    return eventPendingIntent;
                }
                return homePendingIntent;
            case CHECK_IN_SHORT:
            case CHECK_IN_LONG:
                return eventPendingIntent;
            case DISPATCH_ALARM:
                AlarmNotificationSoundPlayer.instance().playSound();
                return eventPendingIntent;
            case JOIN_ALARM:
                notification.defaults |= Notification.DEFAULT_SOUND;
                return eventPendingIntent;
            case STOP_ALARM:
            case STOP_ALARM_REASON_TEST:
            case STOP_ALARM_REASON_ACCIDENTAL:
            case STOP_ALARM_REASON_NOT_NEEDED:
            case STOP_ALARM_REASON_OTHER:
            case START_FOLLOW_ME:
            case STOP_FOLLOW_ME:
            case STOP_FOLLOW_USER:
            case POLICY_NOTIFICATION:
                notification.defaults |= Notification.DEFAULT_SOUND;
                return homePendingIntent;

            default:
                return homePendingIntent;
        }
    }

    private static class GetNotificationsListener implements OnResponseListCallback {

        private Context context;
        private NewNotificationEvent notificationEvent;

        GetNotificationsListener(Context context, NewNotificationEvent notificationEvent) {
            this.context = context;
            this.notificationEvent = notificationEvent;
        }

        @Override
        public void onSuccess(Map<String, List<ParseObject>> objects) {
            ParseObject notificationObject = EventsManager.instance().getEventForId(notificationEvent.getObjectId());

            switch (notificationEvent.getNotificationType()) {
                case JOIN_ALARM:
                    List<String> arguments = notificationEvent.getMessageArgs();
                    EventBusManager.instance().postEvent(new GuardianJoinedEvent(arguments.get(0)));
                    break;
                case ACCEPT_INVITATION:
                case REJECT_INVITATION:
                case CANCEL_INVITATION:
                case REMOVE_CONNECTION_GUARDED:
                    UserManager.instance().getGuardiansForUser(null);
                    break;
                case REMOVE_CONNECTION_GUARDIAN:
                    UserManager.instance().getGuardedUsers(null);
                    break;
                case STOP_ALARM_REASON_TEST:
                    notificationEvent.getMessageArgs().add(context.getString(R.string.reason_test_alarm));
                    break;
                case STOP_ALARM_REASON_ACCIDENTAL:
                    notificationEvent.getMessageArgs().add(context.getString(R.string.reason_accidental_alarm));
                    break;
                case STOP_ALARM_REASON_NOT_NEEDED:
                    notificationEvent.getMessageArgs().add(context.getString(R.string.reason_help_not_needed));
                    break;
                case START_FOLLOW_ME:
                    notificationEvent.getMessageArgs().add(context.getString(R.string.str_start_follow_me_message));
//                    List<String> mStartFollowMe = notificationEvent.getMessageArgs();
//                    if (mStartFollowMe != null && !mStartFollowMe.isEmpty()) {
//                        notificationEvent.getMessageArgs().add(mStartFollowMe.get(0));
//                    }
                    break;

                case STOP_FOLLOW_ME:
                    notificationEvent.getMessageArgs().add(context.getString(R.string.str_stop_follow_me_message));
                    break;

                case STOP_FOLLOW_USER:
                    notificationEvent.getMessageArgs().add(context.getString(R.string.str_stop_follow_user_message));
                    break;

                case POLICY_NOTIFICATION:
                    notificationEvent.getMessageArgs().add(context.getString(R.string.str_policy_notification_msg));
                    break;
            }

            handleNotification(context, notificationEvent, notificationObject);
        }

        @Override
        public void onFailed(Error error) {
            if (error != null) {
                FirebaseCrashlytics.getInstance().log(error.getErrorMessage(context));
            }
        }
    }
}
