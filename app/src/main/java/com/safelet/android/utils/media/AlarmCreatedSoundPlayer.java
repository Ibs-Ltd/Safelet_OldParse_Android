package com.safelet.android.utils.media;

import android.media.MediaPlayer;

import com.safelet.android.R;
import com.safelet.android.global.ApplicationSafelet;


public final class AlarmCreatedSoundPlayer {

    private static final MediaPlayer MEDIA_PLAYER = MediaPlayer.create(ApplicationSafelet.getContext(), R.raw.alarm_created);

    private AlarmCreatedSoundPlayer() {
        throw new UnsupportedOperationException();
    }

    public static void playSound() {
        MEDIA_PLAYER.start();
    }
}
