package com.safelet.android.utils.media;

import android.media.MediaPlayer;

import com.safelet.android.R;
import com.safelet.android.global.ApplicationSafelet;

public final class AlarmNotificationSoundPlayer {

    private MediaPlayer mediaPlayer;

    private static AlarmNotificationSoundPlayer sInstance = null;

    public static AlarmNotificationSoundPlayer instance() {
        if (sInstance == null) {
            sInstance = new AlarmNotificationSoundPlayer();
        }
        return sInstance;
    }

    public void playSound() {
        stopSound();

        mediaPlayer = MediaPlayer.create(ApplicationSafelet.getContext(), R.raw.alarm_notif);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopSound();
            }
        });

        mediaPlayer.start();
    }

    public void stopSound() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
