package com.safelet.android.interactors;

import android.text.TextUtils;

import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.SaveCallback;
import com.safelet.android.R;
import com.safelet.android.interactors.base.BaseManager;
import com.safelet.android.interactors.callbacks.OnAlarmParticipantsCallback;
import com.safelet.android.interactors.callbacks.OnAlarmSoundsCallback;
import com.safelet.android.interactors.callbacks.OnResponseCallback;
import com.safelet.android.interactors.callbacks.SaveRecordSoundCallback;
import com.safelet.android.interactors.utils.ParseConstants;
import com.safelet.android.models.Alarm;
import com.safelet.android.models.AlarmRecordingChunk;
import com.safelet.android.models.StopReason;
import com.safelet.android.models.UserModel;
import com.safelet.android.utils.Error;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlarmManager extends BaseManager {
    private static AlarmManager sInstance;
    private Alarm activeAlarm = null;

    public static AlarmManager instance() {
        if (sInstance == null) {
            sInstance = new AlarmManager();
        }
        return sInstance;
    }

    public void createAlarmForUser(String userId, final OnResponseCallback callback) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ParseConstants.USER_ID_PARAM_KEY, userId);
        ParseCloud.callFunctionInBackground(ParseConstants.CREATE_ALARM_FOR_USER, parameters, new FunctionCallback<Alarm>() {
            @Override
            public void done(Alarm alarm, ParseException e) {
                if (e == null) {
                    try {
                        activeAlarm = alarm;
                        activeAlarm.getUser().fetch();
                    } catch (ParseException ignore) {
                    }
                    callback.onSuccess(activeAlarm);
                } else {
                    callback.onFailed(new Error(e));
                }
            }
        });
    }

    /**
     * <b>NOTE: call this function from background thread</b>
     */
    public Alarm createAlarmForUser(String userId) throws ParseException {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ParseConstants.USER_ID_PARAM_KEY, userId);
        Alarm alarm = ParseCloud.callFunction(ParseConstants.CREATE_ALARM_FOR_USER, parameters);
        activeAlarm = alarm;
        activeAlarm.getUser().fetch();
        return alarm;
    }

    public void getActiveAlarmForUser(String userId, final OnResponseCallback callback) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ParseConstants.USER_ID_PARAM_KEY, userId);
        ParseCloud.callFunctionInBackground(ParseConstants.GET_ACTIVE_ALARM_FOR_USER, parameters, new FunctionCallback<Object>() {
            @Override
            public void done(Object alarm, ParseException e) {
                if (e == null) {
                    if (alarm instanceof Alarm) {
                        try {
                            activeAlarm = (Alarm) alarm;
                            activeAlarm.getUser().fetch();
                            callback.onSuccess(activeAlarm);
                        } catch (ParseException e1) {
                            callback.onFailed(new Error(R.string.error_get_active_alarm));
                        }
                    } else {
                        callback.onFailed(new Error(R.string.error_get_active_alarm));
                    }
                } else {
                    callback.onFailed(new Error(e));
                }
            }
        });
    }

    public void disableCurrentAlarm(StopReason.Reason reason, String description, final OnResponseCallback callback) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ParseConstants.ALARM_ID_PARAM_KEY, activeAlarm.getObjectId());
        parameters.put(ParseConstants.STOP_ALARM_REASON_KEY, reason.getId());
        if (!TextUtils.isEmpty(description)) {
            parameters.put(ParseConstants.STOP_ALARM_DESCRIPTION_KEY, description);
        }
        ParseCloud.callFunctionInBackground(ParseConstants.STOP_ALARM_FOR_USER, parameters, new FunctionCallback<Integer>() {
            @Override
            public void done(Integer integer, ParseException e) {
                if (e == null) {
                    if (integer == SUCCESS_RESPONSE) {
                        activeAlarm.setAlertActive(false);
                        Alarm activeAlarm = getActiveAlarm();
                        AlarmManager.this.activeAlarm = null;
                        callback.onSuccess(activeAlarm);
                    } else {
                        callback.onFailed(new Error(R.string.error_stop_alarm));
                    }
                } else {
                    callback.onFailed(new Error(e));
                }
            }
        });
    }

    public void joinAlarm(final String userId, String alarmId, final OnResponseCallback callback) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ParseConstants.USER_ID_PARAM_KEY, userId);
        parameters.put(ParseConstants.ALARM_ID_PARAM_KEY, alarmId);
        ParseCloud.callFunctionInBackground(ParseConstants.JOIN_ALARM, parameters, new FunctionCallback<Integer>() {
            @Override
            public void done(Integer integer, ParseException e) {
                if (e == null) {
                    if (integer == SUCCESS_RESPONSE) {
                        callback.onSuccess(null);
                    } else {
                        callback.onFailed(new Error(R.string.error_join_alarm));
                    }
                } else {
                    callback.onFailed(new Error(e));
                }
            }
        });
    }

    public void getParticipantsForAlarm(String alarmId, final OnAlarmParticipantsCallback callback) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ParseConstants.ALARM_ID_PARAM_KEY, alarmId);
        ParseCloud.callFunctionInBackground(ParseConstants.GET_PARTICIPANTS_FOR_ALARM, parameters, new FunctionCallback<List<UserModel>>() {
            @Override
            public void done(List<UserModel> participants, ParseException e) {
                if (e == null) {
                    callback.onSuccess(participants);
                } else {
                    callback.onFailed(new Error(e));
                }
            }
        });
    }

    public void updateAlarmFromServer(Alarm alarm, final OnResponseCallback callback) {
        alarm.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    try {
                        ((Alarm) object).getUser().fetch();
                        callback.onSuccess(object);
                    } catch (ParseException ignore) {
                    }
                }
            }
        });
    }

    public void saveAlarmChunkSound(AlarmRecordingChunk alarmRecordingChunk, File chunkFile,
                                    final SaveRecordSoundCallback callback) {
        ParseFile parseFile = new ParseFile(chunkFile);
        try {
            parseFile.save();
        } catch (ParseException e) {
            callback.onFailed(SaveRecordSoundCallback.ERROR_UNKNOWN);
            return;
        }
        alarmRecordingChunk.setChunkFile(parseFile);
        alarmRecordingChunk.setAlarm(activeAlarm);
        alarmRecordingChunk.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (activeAlarm == null) {
                    callback.onFailed(SaveRecordSoundCallback.ERROR_CODE_STOP_RECORDING_ALARM_DISMISSED);
                } else if (!activeAlarm.canRecord()) {
                    callback.onFailed(SaveRecordSoundCallback.ERROR_CODE_STOP_RECORDING);
                } else if (e != null) {
                    callback.onFailed(SaveRecordSoundCallback.ERROR_UNKNOWN);
                } else {
                    callback.onSuccess();
                }
            }
        });
    }

    public void getRecordedChunksSoundForAlarm(String alarmId, final OnAlarmSoundsCallback callback) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ParseConstants.ALARM_ID_PARAM_KEY, alarmId);
        ParseCloud.callFunctionInBackground(ParseConstants.GET_RECORDED_CHUNKS_FOR_ALARM, parameters,
                new FunctionCallback<List<AlarmRecordingChunk>>() {
                    @Override
                    public void done(List<AlarmRecordingChunk> objects, ParseException e) {
                        if (e != null) {
                            callback.onFailed(new Error(e));
                        } else {
                            callback.onSuccess(objects);
                        }
                    }
                });
    }

    public void getLastRecordedChunkSoundForAlarm(String alarmId, final OnResponseCallback callback) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ParseConstants.ALARM_ID_PARAM_KEY, alarmId);
        ParseCloud.callFunctionInBackground(ParseConstants.GET_LAST_RECORDED_CHUNKS_FOR_ALARM, parameters,
                new FunctionCallback<AlarmRecordingChunk>() {
                    @Override
                    public void done(AlarmRecordingChunk object, ParseException e) {
                        if (e != null) {
                            callback.onFailed(new Error(e));
                        } else {
                            callback.onSuccess(object);
                        }
                    }
                });
    }

    public void notifyParticipantsDidCallEmergency(String alarmId, final OnResponseCallback listener) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ParseConstants.ALARM_ID_PARAM_KEY, alarmId);
        ParseCloud.callFunctionInBackground(ParseConstants.NOTIFY_PARTICIPANTS_DID_CALL_EMERGENCY, parameters,
                new FunctionCallback<Integer>() {
                    @Override
                    public void done(Integer integer, ParseException e) {
                        if (e == null && integer != null && integer == SUCCESS_RESPONSE) {
                            listener.onSuccess(null);
                        }
                    }
                });
    }

    public void ignoreAlarm(String alarmId, final OnResponseCallback listener) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ParseConstants.ALARM_ID_PARAM_KEY, alarmId);
        ParseCloud.callFunctionInBackground(ParseConstants.IGNORE_ALARM, parameters,
                new FunctionCallback<Alarm>() {
                    @Override
                    public void done(Alarm integer, ParseException e) {
                        if (e == null) {
                            listener.onSuccess(null);
                        } else {
                            listener.onFailed(new Error(e));
                        }
                    }
                });
    }

    public void updateCurrentAlarm() {
        if (activeAlarm != null) {
            try {
                activeAlarm.fetch();
            } catch (ParseException ignore) {
                // do nothing
            }
        }
    }

    public Alarm getActiveAlarm() {
        return activeAlarm;
    }

    public boolean isActiveAlarm() {
        return activeAlarm != null;
    }

    public void logout() {
        activeAlarm = null;
    }
}
