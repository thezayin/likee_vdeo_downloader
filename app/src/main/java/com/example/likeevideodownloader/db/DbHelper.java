package com.example.likeevideodownloader.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.likeevideodownloader.di.FacebookDownloadEntity;
import com.example.likeevideodownloader.models.FVideo;

import java.util.ArrayList;

public class DbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "weather.db";
    private static final int DATABASE_VERSION = 5;

    public DbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_TABLE_FACEBOOK_DOWNLOADS =
                "CREATE TABLE " + FacebookDownloadEntity.TABLE_NAME + " (" +
                        FacebookDownloadEntity.COLUMN_DOWNLOAD_ID + " LONG PRIMARY KEY, " +
                        FacebookDownloadEntity.COLUMN_FILE_NAME + " TEXT NOT NULL, " +
                        FacebookDownloadEntity.COLUMN_FILE_URI + " TEXT, " +
                        FacebookDownloadEntity.COLUMN_OUTPUT_PATH + " TEXT, " +
                        FacebookDownloadEntity.COLUMN_IS_WATERMARKED + " BOOLEAN, " +
                        FacebookDownloadEntity.COLUMN_STATE + " INTEGER, " +
                        FacebookDownloadEntity.COLUMN_VIDEO_SOURCE + " INTEGER, " +
                        FacebookDownloadEntity.COLUMN_DOWNLOAD_TIME + " LONG NOT NULL " +
                        ");";

        db.execSQL(SQL_CREATE_TABLE_FACEBOOK_DOWNLOADS);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FacebookDownloadEntity.TABLE_NAME);
        onCreate(db);
    }

    public boolean insertVideo(FVideo video, int count) {
        SQLiteDatabase db = this.getWritableDatabase();


        ContentValues contentValues = new ContentValues();

        contentValues.put(FacebookDownloadEntity.COLUMN_DOWNLOAD_ID, video.getDownloadId());
        contentValues.put(FacebookDownloadEntity.COLUMN_FILE_NAME, video.getFileName());
        contentValues.put(FacebookDownloadEntity.COLUMN_FILE_URI, video.getFileUri());
        contentValues.put(FacebookDownloadEntity.COLUMN_IS_WATERMARKED, video.isWatermarked());
        contentValues.put(FacebookDownloadEntity.COLUMN_OUTPUT_PATH, video.getOutputPath());
        contentValues.put(FacebookDownloadEntity.COLUMN_STATE, video.getState());
        contentValues.put(FacebookDownloadEntity.COLUMN_VIDEO_SOURCE, video.getVideoSource());
        contentValues.put(FacebookDownloadEntity.COLUMN_DOWNLOAD_TIME, video.getDownloadTime());


        if (count == 10) {
            String query = "DELETE FROM " + FacebookDownloadEntity.TABLE_NAME + " WHERE " +
                    FacebookDownloadEntity.COLUMN_DOWNLOAD_TIME +
                    " = (SELECT MIN( " + FacebookDownloadEntity.COLUMN_DOWNLOAD_TIME + ") FROM " +
                    FacebookDownloadEntity.TABLE_NAME + ")";

            db.execSQL(query);
        }
        db.insert(FacebookDownloadEntity.TABLE_NAME, null, contentValues);
        return true;
    }

    public ArrayList<FVideo> getAllVideos() {
        ArrayList<FVideo> array_list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + FacebookDownloadEntity.TABLE_NAME +
                        " ORDER BY " + FacebookDownloadEntity.COLUMN_DOWNLOAD_TIME + " DESC",
                null);
        res.moveToFirst();

        while (!res.isAfterLast()) {
            FVideo video = new FVideo(res.getLong(FacebookDownloadEntity.INDEX_DOWNLOAD_TIME));
            video.setFileName(res.getString(FacebookDownloadEntity.INDEX_FILE_NAME));
            video.setState(res.getInt(FacebookDownloadEntity.INDEX_STATE));
            video.setFileUri(res.getString(FacebookDownloadEntity.INDEX_FILE_URI));

            video.setDownloadId(res.getLong(FacebookDownloadEntity.INDEX_DOWNLOAD_ID));
            video.setOutputPath(res.getString(FacebookDownloadEntity.INDEX_OUTPUT_PATH));
            video.setVideoSource(res.getInt(FacebookDownloadEntity.INDEX_VIDEO_SOURCE));

            array_list.add(video);

            res.moveToNext();
        }
        return array_list;
    }

    public ArrayList<FVideo> getRecentVideos() {
        ArrayList<FVideo> array_list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + FacebookDownloadEntity.TABLE_NAME +
                        " ORDER BY " + FacebookDownloadEntity.COLUMN_DOWNLOAD_TIME + " DESC LIMIT 10",
                null);
        res.moveToFirst();

        while (!res.isAfterLast()) {
            FVideo video = new FVideo(res.getLong(FacebookDownloadEntity.INDEX_DOWNLOAD_TIME));
            video.setFileName(res.getString(FacebookDownloadEntity.INDEX_FILE_NAME));
            video.setState(res.getInt(FacebookDownloadEntity.INDEX_STATE));
            video.setFileUri(res.getString(FacebookDownloadEntity.INDEX_FILE_URI));

            video.setDownloadId(res.getLong(FacebookDownloadEntity.INDEX_DOWNLOAD_ID));
            video.setOutputPath(res.getString(FacebookDownloadEntity.INDEX_OUTPUT_PATH));
            video.setVideoSource(res.getInt(FacebookDownloadEntity.INDEX_VIDEO_SOURCE));

            array_list.add(video);

            res.moveToNext();
        }
        return array_list;
    }

    public ArrayList<FVideo> getFacebookVideos() {
        ArrayList<FVideo> array_list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + FacebookDownloadEntity.TABLE_NAME +
                        " where " + FacebookDownloadEntity.COLUMN_VIDEO_SOURCE + "= " + FVideo.FACEBOOK +
                        " ORDER BY " + FacebookDownloadEntity.COLUMN_DOWNLOAD_TIME + " DESC ",
                null);
        res.moveToFirst();

        while (!res.isAfterLast()) {
            FVideo video = new FVideo(res.getLong(FacebookDownloadEntity.INDEX_DOWNLOAD_TIME));
            video.setFileName(res.getString(FacebookDownloadEntity.INDEX_FILE_NAME));
            video.setState(res.getInt(FacebookDownloadEntity.INDEX_STATE));
            video.setFileUri(res.getString(FacebookDownloadEntity.INDEX_FILE_URI));

            video.setDownloadId(res.getLong(FacebookDownloadEntity.INDEX_DOWNLOAD_ID));
            video.setOutputPath(res.getString(FacebookDownloadEntity.INDEX_OUTPUT_PATH));
            video.setVideoSource(res.getInt(FacebookDownloadEntity.INDEX_VIDEO_SOURCE));

            array_list.add(video);

            res.moveToNext();
        }
        return array_list;
    }

    public FVideo getVideo(long downloadId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + FacebookDownloadEntity.TABLE_NAME +
                " where " + FacebookDownloadEntity.COLUMN_DOWNLOAD_ID + " = " + downloadId, null);

        if (res == null || res.getCount() == 0 || res.getColumnCount() == 0)
            return null;

        res.moveToFirst();
        FVideo video = new FVideo(res.getLong(FacebookDownloadEntity.INDEX_DOWNLOAD_TIME));
        video.setFileName(res.getString(FacebookDownloadEntity.INDEX_FILE_NAME));
        video.setState(res.getInt(FacebookDownloadEntity.INDEX_STATE));
        video.setFileUri(res.getString(FacebookDownloadEntity.INDEX_FILE_URI));
        video.setIsWatermarked(res.getInt(FacebookDownloadEntity.INDEX_IS_WATERMARKED) > 0);

        video.setDownloadId(res.getLong(FacebookDownloadEntity.INDEX_DOWNLOAD_ID));
        video.setOutputPath(res.getString(FacebookDownloadEntity.INDEX_OUTPUT_PATH));
        video.setVideoSource(res.getInt(FacebookDownloadEntity.INDEX_VIDEO_SOURCE));

        return video;
    }

    public int deleteVideo(long downloadId) {

        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(FacebookDownloadEntity.TABLE_NAME,
                FacebookDownloadEntity.COLUMN_DOWNLOAD_ID + " = ? ",
                new String[]{Long.toString(downloadId)});
    }

    public void updateState(long downloadId, int state) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("UPDATE " + FacebookDownloadEntity.TABLE_NAME +
                " SET " + FacebookDownloadEntity.COLUMN_STATE + " = " + state +
                " WHERE " + FacebookDownloadEntity.COLUMN_DOWNLOAD_ID + " = " + downloadId);
        db.close();

    }

    public void setUri(long downloadId, String uri) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + FacebookDownloadEntity.TABLE_NAME +
                " SET " + FacebookDownloadEntity.COLUMN_FILE_URI + " = '" + uri +
                "' WHERE " + FacebookDownloadEntity.COLUMN_DOWNLOAD_ID + " = " + downloadId);
        db.close();
    }

}
