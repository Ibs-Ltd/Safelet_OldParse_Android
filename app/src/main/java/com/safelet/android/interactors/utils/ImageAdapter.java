package com.safelet.android.interactors.utils;

import android.net.Uri;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class ImageAdapter extends TypeAdapter<Uri> {

    @Override
    public void write(JsonWriter jsonWriter, Uri value) throws IOException {
        jsonWriter.value(value.toString());
    }

    @Override
    public Uri read(JsonReader jsonReader) throws IOException {
        switch (jsonReader.peek()) {
            case STRING:
                return Uri.parse(jsonReader.nextString());

            default:
                throw new RuntimeException("Expected string, not " + jsonReader.peek());
        }
    }
}
