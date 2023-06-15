package com.example.likeevideodownloader.models;

import java.util.ArrayList;

public class LikeeVideo {
    public boolean error;
    public String msg;
    public ArrayList<Data> data;

    public static class Data {
        public String url;
        public String format;
        public String ext;
        public String format_id;
    }

}
