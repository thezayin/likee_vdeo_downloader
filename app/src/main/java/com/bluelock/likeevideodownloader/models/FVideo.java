package com.bluelock.likeevideodownloader.models;

public class FVideo {
    public static final int DOWNLOADING = 1;
    public static final int PROCESSING = 2;
    public static final int COMPLETE = 3;

    public static final int FACEBOOK = 1;
    public static final int WHATSAPP = 3;
    public static final int INSTAGRAM = 2;
    public static final int SNAPCHAT = 4;
    public static final int LIKEE = 5;
    public static final int MOZ = 6;
    private boolean isWatermarked;
    //Initial download path
    private String outputPath;
    private String fileName;
    private long downloadId;
    //Where file actually saved in memory after processing
    private String fileUri;
    private int State;

    private int videoSource;
    private long downloadTime;

    public FVideo(String outputPath, String fileName, long downloadId, boolean isWatermarked, long downloadTime) {
        this.outputPath = outputPath;
        this.fileName = fileName;
        this.downloadId = downloadId;
        this.isWatermarked = isWatermarked;
        this.downloadTime = downloadTime;
    }

    public FVideo(long downloadTime) {
        this.downloadTime = downloadTime;
    }

    public void setIsWatermarked(Boolean watermarked) {
        isWatermarked = watermarked;
    }

    public boolean isWatermarked() {
        return isWatermarked;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(long downloadId) {
        this.downloadId = downloadId;
    }

    public String getFileUri() {
        return fileUri;
    }

    public void setFileUri(String fileUri) {
        this.fileUri = fileUri;
    }

    public int getState() {
        return State;
    }

    public void setState(int state) {
        State = state;
    }

    public int getVideoSource() {
        return videoSource;
    }

    public void setVideoSource(int videoSource) {
        this.videoSource = videoSource;
    }

    public long getDownloadTime() {
        return downloadTime;
    }

    public void setDownloadTime(long downloadTime) {
        this.downloadTime = downloadTime;
    }
}
