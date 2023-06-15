package com.example.likeevideodownloader.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class FacebookVideo {
    @SerializedName("error")
    public boolean error;
    @SerializedName("msg")
    public String msg;
    @SerializedName("data")
    public ArrayList<Data> data;

    public static class Data {
        @SerializedName("url")
        public String url;
        @SerializedName("format")
        public String format;
        @SerializedName("ext")
        public String ext;
        @SerializedName("format_id")
        public String format_id;
    }
}
