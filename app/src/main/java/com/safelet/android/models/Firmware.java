package com.safelet.android.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

@ParseClassName("Firmware")
public class Firmware extends ParseObject {
    private static final String VERSION_NAME_KEY = "versionname";
    private static final String VERSION_CODE_KEY = "versioncode";
    private static final String UPDATE_FILE_KEY = "updatefile";

    public Firmware() {
    }

    public String getVersionName() {
        return getString(VERSION_NAME_KEY);
    }

    public int getVersionCode() {
        return getInt(VERSION_CODE_KEY);
    }

    public ParseFile getUpdateFile() {
        return getParseFile(UPDATE_FILE_KEY);
    }
}
