package com.safelet.android.interactors.utils;

public class ParseConstants {

    // Parse services
    public static final String CHECK_IF_USER_EXIST = "checkIfUserExists";
    public static final String GET_GUARDIANS_FOR_USER = "fetchListOfGuardiansForUser";
    public static final String GET_GUARDIAN_USER = "fetchListOfUsersGuardedByUser";
    public static final String REMOVE_GUARDIAN_FOR_USER = "removeGuardianConnection";
    public static final String GET_USERS_FOR_PHONE = "fetchParseUsersForPhoneUsers";
    public static final String SEND_USER_PHONE_CONTACTS = "syncUserPhoneContacts";
    public static final String SEND_GUARDIAN_REQUEST = "sendGuardianRequest";
    public static final String GET_LAST_CHECKIN_FOR_USER = "fetchLastCheckInForUser";
    public static final String CREATE_CHECKIN_FOR_USER = "createCheckInForUser";
    public static final String CREATE_ALARM_FOR_USER = "dispatchAlarmForUser";
    public static final String GET_ACTIVE_ALARM_FOR_USER = "fetchActiveAlarmForUser";
    public static final String GET_EVENTS_FOR_USER = "fetchNotificationsForUser";
    public static final String STOP_ALARM_FOR_USER = "stopAlarm";
    public static final String JOIN_ALARM = "joinAlarm";
    public static final String GET_PARTICIPANTS_FOR_ALARM = "fetchParticipantsForAlarm";
    public static final String RESPONSE_TO_INVITATION = "respondToGuardianInvitation";
    public static final String CANCEL_INVITATION = "cancelGuardianInvitation";
    public static final String CREATE_NON_USER_INVITATION = "createNonUserSMSInvitation";
    public static final String REMOVE_NON_USER_INVITATION = "removeNonUserSMSInvitation";
    public static final String GET_RECORDED_CHUNKS_FOR_ALARM = "fetchAllRecordingChunksForAlarm";
    public static final String GET_LAST_RECORDED_CHUNKS_FOR_ALARM = "fetchLastRecordingChunkForAlarm";
    public static final String NOTIFY_PARTICIPANTS_DID_CALL_EMERGENCY = "notifyParticipantsDidCallEmergency";
    public static final String IGNORE_ALARM = "ignoreAlarm";
    public static final String SAVE_PHONE_DETAILS = "savePhoneDetails";
    public static final String BLUETOOTH_CONNECTION_FAILED = "markBluetoothConnectionFailed";
    public static final String GET_LATEST_FIRMWARE = "getLatestFirmware";
    public static final String GET_FIREBASE_TOKEN = "getFirebaseToken";
    public static final String DELETE_MY_ACCOUNT = "deleteMyAccount";
    public static final String CREATE_CHECK_IN_FOR_MULTIPLE_USER = "createCheckInForMulitipleUser";
    public static final String START_FOLLOW_ME_TO_MULTIPLE_GUARDIANS = "startFollowMeToMulitipleGuardians";
    public static final String STOP_MULTIPLE_FOLLOW_ME_USER = "stopFollowMe";
    public static final String GET_FOLLOw_ME_USERS = "getFollowMeGuardians";


    // Parse parameters keys
    public static final String USER_ID_PARAM_KEY = "aUserObjectId";
    public static final String FROM_USER_PARAM_KEY = "fromUser";
    public static final String TO_USER_PARAM_KEY = "toUser";
    public static final String INIT_USER_ID_PARAM_KEY = "initiatingUserObjectId";
    public static final String EMAILS_PARAM_KEY = "emails";
    public static final String PHONES_PARAM_KEY = "phoneNumbers";
    public static final String PHONE_PARAM_KEY = "phoneNumber";
    public static final String CONTACT_NAME_PARAM_KEY = "contactName";
    public static final String CONTACTS_PARAM_KEY = "contacts";
    public static final String OTHER_USERS_PARAM_KEY = "otherUsers";
    public static final String CHECKIN_GEOPOINT_PARAM_KEY = "checkInGeoPoint";
    public static final String CHECKIN_ADDRESS_PARAM_KEY = "checkInAddress";
    public static final String CHECKIN_MESSAGE_PARAM_KEY = "checkInMessage";
    public static final String ALARM_ID_PARAM_KEY = "alarmObjectId";
    public static final String STATUS_PARAM_KEY = "status";
    public static final String DEVICE_PARAM_KEY = "device";
    public static final String MODEL_PARAM_KEY = "model";
    public static final String OS_VERSION_PARAM_KEY = "osVersion";
    public static final String APP_VERSION_PARAM_KEY = "appVersion";
    public static final String LOG_FILE_URL_PARAM_KEY = "logFileURL";
    public static final String MODEL_KEY = "model";
    public static final String HARDWARE_REVISION_KEY = "hardwareRevision";
    public static final String FIRMWARE_REVISION_KEY = "firmwareRevision";
    public static final String VERSION_CODE_KEY = "versionCode";
    public static final String HISTORIC_EVENTS_KEY = "includeHistoricEvents";
    public static final String STOP_ALARM_REASON_ID_KEY = "reason";
    public static final String STOP_ALARM_OTHER_DESCRIPTION_KEY = "otherReasonDescription";
    public static final String STOP_ALARM_REASON_KEY = "stopAlarmReason";
    public static final String STOP_ALARM_DESCRIPTION_KEY = "stopReasonDescription";
    public static final String SELECTED_USERS_OBJECT_IDS_KEY = "selectedUsersObjectIds";

    // Parse objects key
    public static final String USER_SENT_INVITATION_KEY = "userSentInvitationStatus";
}
