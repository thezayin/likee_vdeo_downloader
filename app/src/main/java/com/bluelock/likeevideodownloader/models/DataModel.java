package com.bluelock.likeevideodownloader.models;

import android.os.Parcel;
import android.os.Parcelable;


public class DataModel implements Parcelable {
    public static final Creator<DataModel> CREATOR = new Creator<DataModel>() {
        @Override
        public DataModel createFromParcel(Parcel in) {
            return new DataModel(in);
        }

        @Override
        public DataModel[] newArray(int size) {
            return new DataModel[size];
        }
    };
    private String filename;
    private String filepath;

    public DataModel(String paramString1, String paramString2) {
        this.filepath = paramString1;
        this.filename = paramString2;
    }

    protected DataModel(Parcel in) {
        filename = in.readString();
        filepath = in.readString();
    }

    public String getFileName() {
        return this.filename;
    }

    public void setFileName(String paramString) {
        this.filename = paramString;
    }

    public String getFilePath() {
        return this.filepath;
    }

    public void setFilePath(String paramString) {
        this.filepath = paramString;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(filename);
        parcel.writeString(filepath);
    }
}
