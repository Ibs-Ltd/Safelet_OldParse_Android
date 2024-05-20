package com.safelet.android.models;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class Message {

    @SerializedName("text")
    private String text;

    @SerializedName("sender")
    private User sender;

    @SerializedName("timestamp")
    private long timestamp;

    private Type type;

    public Message() {
    }

    public Message(@NonNull String text, @NonNull User sender) {
        this.text = text;
        this.sender = sender;
        this.timestamp = System.currentTimeMillis();
    }

    @NonNull
    public String getText() {
        return text;
    }

    @NonNull
    public User getSender() {
        return sender;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        INCOMING, OUTGOING
    }
}
