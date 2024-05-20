package com.safelet.android.models.event;

import com.safelet.android.models.AlarmRecordingChunk;
import com.safelet.android.utils.Error;

import java.util.List;

public class ChunkFileReceivedEvent extends BaseEvent {

    private List<AlarmRecordingChunk> results;

    public ChunkFileReceivedEvent(List<AlarmRecordingChunk> results) {
        this.results = results;
    }

    public ChunkFileReceivedEvent(Error error) {
        super(error);
    }

    public List<AlarmRecordingChunk> getResults() {
        return results;
    }
}
