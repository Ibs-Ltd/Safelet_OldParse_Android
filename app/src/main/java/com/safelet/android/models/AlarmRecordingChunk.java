package com.safelet.android.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

@ParseClassName("AlarmRecordingChunk")
public class AlarmRecordingChunk extends ParseObject {

    private static final String ALARM_KEY = "alarm";
    private static final String CHUNK_FILE_KEY = "chunkFile";

    public AlarmRecordingChunk() {
    }

    public Alarm getAlarm() {
        return (Alarm) getParseObject(ALARM_KEY);
    }

    public void setAlarm(Alarm alarm) {
        if (alarm != null) {
            put(ALARM_KEY, alarm);
        }
    }

    public ParseFile getChunkFile() {
        return getParseFile(CHUNK_FILE_KEY);
    }

    public void setChunkFile(ParseFile chunkFile) {
        if (chunkFile != null) {
            put(CHUNK_FILE_KEY, chunkFile);
        }
    }
}
