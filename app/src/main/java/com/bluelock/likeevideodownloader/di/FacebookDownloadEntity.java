package com.bluelock.likeevideodownloader.di;

import android.provider.BaseColumns;

public class FacebookDownloadEntity implements BaseColumns {

    public static final String TABLE_NAME = "facebook_downloads";
    public static final String COLUMN_IS_WATERMARKED = "isWatermarked";
    public static final String COLUMN_OUTPUT_PATH = "outputPath";
    public static final String COLUMN_FILE_NAME = "fileName";
    public static final String COLUMN_DOWNLOAD_ID = "downloadId";
    public static final String COLUMN_FILE_URI = "fileUri";
    public static final String COLUMN_STATE = "state";
    public static final String COLUMN_VIDEO_SOURCE = "source";
    public static final String COLUMN_DOWNLOAD_TIME = "time";

    public static final int INDEX_DOWNLOAD_ID = 0;
    public static final int INDEX_FILE_NAME = 1;
    public static final int INDEX_FILE_URI = 2;
    public static final int INDEX_OUTPUT_PATH = 3;
    public static final int INDEX_IS_WATERMARKED = 4;
    public static final int INDEX_STATE = 5;
    public static final int INDEX_VIDEO_SOURCE = 6;
    public static final int INDEX_DOWNLOAD_TIME = 7;
}
