package com.safelet.android.models;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("number")
    private String number;

    @SerializedName("image")
    private String image;

    public User() {
    }

    public User(@NonNull String id, @NonNull String name, @NonNull String number, String image) {
        this.id = id;
        this.number = number;
        this.name = name;
        this.image = image;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NonNull
    public String getNumber() {
        return number;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
