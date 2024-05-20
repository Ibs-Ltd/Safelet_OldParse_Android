package com.safelet.android.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.parse.ParseObject;
import com.safelet.android.activities.base.BaseActivity;
import com.safelet.android.interactors.AlarmManager;
import com.safelet.android.interactors.callbacks.SaveRecordSoundCallback;
import com.safelet.android.models.AlarmRecordingChunk;

import java.io.File;
import java.io.IOException;

import timber.log.Timber;

/**
 *
 */
public class RecordSoundService extends Service implements MediaRecorder.OnInfoListener {

    public static final String RECORDING_STATUS_CHANGED = "rss.recordingStatusChanged.intent";
    private static final String SOUND_NAME = "sound";
    private static final int AUDIO_RECORDED_SIZE = 10000; // 10 seconds
    private static final int RESTART_DELAY_IN_CASE_OF_EXCEPTION = 5000; // 5 seconds

    private MediaRecorder recorder = null;
    private File chunkFile;

    @RequiresPermission("android.permission.RECORD_AUDIO")
    public static void startRecording(Context context) {
        Intent recordingIntent = new Intent(context, RecordSoundService.class);
        context.startService(recordingIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("".concat(BaseActivity.TAG).concat("RecordSoundService ").concat("onStartCommand"));
        if (recorder == null) {
            try {
                startRecording();
            } catch (IOException ignored) {
                // do nothing
            }
        }
        return START_NOT_STICKY;
    }

    private void startRecording() throws IOException {
        Timber.d("".concat(BaseActivity.TAG).concat("RecordSoundService ").concat("startRecording"));
        if (recorder == null) {
            recorder = new MediaRecorder();
        }
        try {
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);

            chunkFile = new File(getCacheDir(), SOUND_NAME + ".aac");
            Timber.tag(BaseActivity.TAG).d("".concat("get Absolute Path: ").concat(chunkFile.getAbsolutePath()));
            if (!chunkFile.exists()) {
                chunkFile.createNewFile();
            } else {
                chunkFile.delete();
                chunkFile.createNewFile();
            }

            recorder.setOutputFile(chunkFile.getAbsolutePath());
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setMaxDuration(AUDIO_RECORDED_SIZE);
            recorder.setOnInfoListener(this);
            recorder.prepare();
            recorder.start();
        } catch (IllegalStateException e) {
            // restart recording in case of exception
            restartRecordingWithDelay(RESTART_DELAY_IN_CASE_OF_EXCEPTION);
        }
    }

    private void stopRecording() {
        Timber.d("".concat(BaseActivity.TAG).concat("RecordSoundService ").concat("stopRecording"));
        if (recorder != null) {
            try {
                recorder.stop();
                recorder.release();
            } catch (RuntimeException ignore) {
                // do nothing, happened in case the service is destroyed before to start recording
            }
            recorder = null;
        }
    }

    private void restartRecordingWithDelay(int delay) {
        Timber.d("".concat(BaseActivity.TAG).concat("RecordSoundService ").concat("restartRecordingWithDelay"));
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (recorder != null) {
                    recorder.reset();
                }
                recorder = null;
                try {
                    startRecording();
                } catch (IOException ignore) {
                }
            }
        }, delay);
    }

    private void uploadFileRecorded() {
        Timber.d("".concat(BaseActivity.TAG).concat("RecordSoundService ").concat("uploadFileRecorded"));
        AlarmManager alarmManager = AlarmManager.instance();
        if (alarmManager.isActiveAlarm()) {
            AlarmRecordingChunk alarmRecordingChunk = ParseObject.create(AlarmRecordingChunk.class);
            alarmManager.saveAlarmChunkSound(alarmRecordingChunk, chunkFile, new UploadAudioChunkFileListener());
        }
    }

    @Override
    public void onDestroy() {
        Timber.d("".concat(BaseActivity.TAG).concat("RecordSoundService ").concat("onDestroy"));
        stopRecording();
        super.onDestroy();
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        Timber.d("".concat(BaseActivity.TAG).concat("RecordSoundService ").concat("onInfo"));
        Timber.d("".concat(BaseActivity.TAG).concat("onInfo: ").concat(String.valueOf(what)));
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            stopRecording();
            uploadFileRecorded();
        }
    }

    private class UploadAudioChunkFileListener implements SaveRecordSoundCallback {

        @Override
        public void onSuccess() {
            try {
                startRecording();
                AlarmManager.instance().updateCurrentAlarm();
            } catch (IOException ignore) {
            }
        }

        @Override
        public void onFailed(int errorCode) {
            if (errorCode == ERROR_CODE_STOP_RECORDING_ALARM_DISMISSED) {
                stopSelf();
            } else if (errorCode == ERROR_CODE_STOP_RECORDING) {
                Intent stopRecordingIntent = new Intent(RECORDING_STATUS_CHANGED);
                LocalBroadcastManager.getInstance(RecordSoundService.this).sendBroadcast(stopRecordingIntent);
                stopSelf();
            } else {
                try {
                    startRecording();
                } catch (IOException ignore) {
                }
            }
        }
    }
}
