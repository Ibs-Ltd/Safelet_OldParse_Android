package com.safelet.android.interactors.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.safelet.android.activities.base.BaseActivity;
import com.safelet.android.models.AlarmRecordingChunk;
import com.safelet.android.utils.log.TimberDecorator;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 *
 */
public class ChunkSoundsPlayer implements Player.EventListener {

    private static final String TAG = ChunkSoundsPlayer.class.getSimpleName();

    private static final String SAFELET_USER_AGENT = "Safelet";

    private SimpleExoPlayer exoPlayer;
    private DataSource.Factory dataSourceFactory;

    private List<AlarmRecordingChunk> chunkList = new ArrayList<>();
    private int chunkIndex = 0;

    private boolean isSoundPlaying = false;
    private boolean userSetPlaying = false;

    private OnChunkPlayListener listener;

    public ChunkSoundsPlayer(Context context, OnChunkPlayListener listener) {
        this.listener = listener;

        exoPlayer = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(context), new DefaultTrackSelector(new DefaultBandwidthMeter()),
                new DefaultLoadControl());
        exoPlayer.setPlayWhenReady(true);
        exoPlayer.addListener(this);

        dataSourceFactory = new DefaultDataSourceFactory(context, SAFELET_USER_AGENT);
    }

    public void setChunkList(List<AlarmRecordingChunk> chunkList) {
        this.chunkList = chunkList;
    }

    public void addChunk(AlarmRecordingChunk chunk) {
        boolean exist = false;
        for (AlarmRecordingChunk alarmRecordingChunk : chunkList) {
            if (chunk.getObjectId().equals(alarmRecordingChunk.getObjectId())) {
                exist = true;
                break;
            }
        }
        if (!exist) {
            TimberDecorator.d(TAG, "add chunk " + userSetPlaying + " " + isSoundPlaying);
            chunkList.add(chunk);
            if (userSetPlaying && !isSoundPlaying) {
                play();
            }
        }
    }

    public void setListener(OnChunkPlayListener chunkPlayListener) {
        listener = chunkPlayListener;
    }

    public void play() {
        if (chunkIndex < chunkList.size()) {
            TimberDecorator.d(TAG, "play " + chunkIndex);
            exoPlayer.prepare(new ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(
                            chunkList.get(chunkIndex).getChunkFile().getUrl())));
            userSetPlaying = true;
            Timber.e("".concat("Play Recording URL: ").concat(chunkList.get(chunkIndex).getChunkFile().getUrl()));
        } else {
            if (listener != null) {
                listener.onStopPlaying();
            }
        }
    }

    public void stop() {
        TimberDecorator.d(TAG, "stop playing");
        userSetPlaying = false;
        chunkIndex = 0;
        exoPlayer.stop();
        if (listener != null) {
            listener.onStopPlaying();
        }
    }

    public boolean isPlaying() {
        return userSetPlaying;
    }

    public void destroy() {
        exoPlayer.release();
        exoPlayer = null;
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == Player.STATE_ENDED) {
            TimberDecorator.d(TAG, "onStop " + chunkIndex + " " + chunkList.size() + " " + userSetPlaying);
            isSoundPlaying = false;
            if (userSetPlaying) {
                chunkIndex++;
                play();
            }
        } else if (playbackState == Player.STATE_READY) {
            TimberDecorator.d(TAG, "on start");
            if (listener != null) {
                listener.onStartPlaying();
            }
            isSoundPlaying = true;
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        isSoundPlaying = false;
        userSetPlaying = false;
        if (listener != null) {
            listener.onStopPlaying();
        }
        TimberDecorator.d(TAG, "onError");
    }

    @Override
    public void onPositionDiscontinuity(int reason) {
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
    }

    @Override
    public void onSeekProcessed() {
    }

    public interface OnChunkPlayListener {
        void onStartPlaying();

        void onStopPlaying();
    }
}
