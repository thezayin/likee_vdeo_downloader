package com.bluelock.likeevideodownloader.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.bluelock.likeevideodownloader.di.FacebookDownloadEntity
import com.bluelock.likeevideodownloader.models.FVideo

class DbHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        val SQL_CREATE_TABLE_FACEBOOK_DOWNLOADS =
            "CREATE TABLE " + FacebookDownloadEntity.TABLE_NAME + " (" +
                    FacebookDownloadEntity.COLUMN_DOWNLOAD_ID + " LONG PRIMARY KEY, " +
                    FacebookDownloadEntity.COLUMN_FILE_NAME + " TEXT NOT NULL, " +
                    FacebookDownloadEntity.COLUMN_FILE_URI + " TEXT, " +
                    FacebookDownloadEntity.COLUMN_OUTPUT_PATH + " TEXT, " +
                    FacebookDownloadEntity.COLUMN_IS_WATERMARKED + " BOOLEAN, " +
                    FacebookDownloadEntity.COLUMN_STATE + " INTEGER, " +
                    FacebookDownloadEntity.COLUMN_VIDEO_SOURCE + " INTEGER, " +
                    FacebookDownloadEntity.COLUMN_DOWNLOAD_TIME + " LONG NOT NULL " +
                    ");"
        db.execSQL(SQL_CREATE_TABLE_FACEBOOK_DOWNLOADS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + FacebookDownloadEntity.TABLE_NAME)
        onCreate(db)
    }

    fun insertVideo(video: FVideo, count: Int): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(FacebookDownloadEntity.COLUMN_DOWNLOAD_ID, video.downloadId)
        contentValues.put(FacebookDownloadEntity.COLUMN_FILE_NAME, video.fileName)
        contentValues.put(FacebookDownloadEntity.COLUMN_FILE_URI, video.fileUri)
        contentValues.put(FacebookDownloadEntity.COLUMN_IS_WATERMARKED, video.isWatermarked)
        contentValues.put(FacebookDownloadEntity.COLUMN_OUTPUT_PATH, video.outputPath)
        contentValues.put(FacebookDownloadEntity.COLUMN_STATE, video.state)
        contentValues.put(FacebookDownloadEntity.COLUMN_VIDEO_SOURCE, video.videoSource)
        contentValues.put(FacebookDownloadEntity.COLUMN_DOWNLOAD_TIME, video.downloadTime)
        if (count == 10) {
            val query = "DELETE FROM " + FacebookDownloadEntity.TABLE_NAME + " WHERE " +
                    FacebookDownloadEntity.COLUMN_DOWNLOAD_TIME +
                    " = (SELECT MIN( " + FacebookDownloadEntity.COLUMN_DOWNLOAD_TIME + ") FROM " +
                    FacebookDownloadEntity.TABLE_NAME + ")"
            db.execSQL(query)
        }
        db.insert(FacebookDownloadEntity.TABLE_NAME, null, contentValues)
        return true
    }

    val allVideos: ArrayList<FVideo>
        get() {
            val array_list = ArrayList<FVideo>()
            val db = this.readableDatabase
            val res = db.rawQuery(
                "select * from " + FacebookDownloadEntity.TABLE_NAME +
                        " ORDER BY " + FacebookDownloadEntity.COLUMN_DOWNLOAD_TIME + " DESC",
                null
            )
            res.moveToFirst()
            while (!res.isAfterLast) {
                val video = FVideo(res.getLong(FacebookDownloadEntity.INDEX_DOWNLOAD_TIME))
                video.fileName = res.getString(FacebookDownloadEntity.INDEX_FILE_NAME)
                video.state = res.getInt(FacebookDownloadEntity.INDEX_STATE)
                video.fileUri = res.getString(FacebookDownloadEntity.INDEX_FILE_URI)
                video.downloadId = res.getLong(FacebookDownloadEntity.INDEX_DOWNLOAD_ID)
                video.outputPath = res.getString(FacebookDownloadEntity.INDEX_OUTPUT_PATH)
                video.videoSource = res.getInt(FacebookDownloadEntity.INDEX_VIDEO_SOURCE)
                array_list.add(video)
                res.moveToNext()
            }
            return array_list
        }
    val recentVideos: ArrayList<FVideo>
        get() {
            val array_list = ArrayList<FVideo>()
            val db = this.readableDatabase
            val res = db.rawQuery(
                "select * from " + FacebookDownloadEntity.TABLE_NAME +
                        " ORDER BY " + FacebookDownloadEntity.COLUMN_DOWNLOAD_TIME + " DESC LIMIT 10",
                null
            )
            res.moveToFirst()
            while (!res.isAfterLast) {
                val video = FVideo(res.getLong(FacebookDownloadEntity.INDEX_DOWNLOAD_TIME))
                video.fileName = res.getString(FacebookDownloadEntity.INDEX_FILE_NAME)
                video.state = res.getInt(FacebookDownloadEntity.INDEX_STATE)
                video.fileUri = res.getString(FacebookDownloadEntity.INDEX_FILE_URI)
                video.downloadId = res.getLong(FacebookDownloadEntity.INDEX_DOWNLOAD_ID)
                video.outputPath = res.getString(FacebookDownloadEntity.INDEX_OUTPUT_PATH)
                video.videoSource = res.getInt(FacebookDownloadEntity.INDEX_VIDEO_SOURCE)
                array_list.add(video)
                res.moveToNext()
            }
            return array_list
        }
    val facebookVideos: ArrayList<FVideo>
        get() {
            val array_list = ArrayList<FVideo>()
            val db = this.readableDatabase
            val res = db.rawQuery(
                "select * from " + FacebookDownloadEntity.TABLE_NAME +
                        " where " + FacebookDownloadEntity.COLUMN_VIDEO_SOURCE + "= " + FVideo.FACEBOOK +
                        " ORDER BY " + FacebookDownloadEntity.COLUMN_DOWNLOAD_TIME + " DESC ",
                null
            )
            res.moveToFirst()
            while (!res.isAfterLast) {
                val video = FVideo(res.getLong(FacebookDownloadEntity.INDEX_DOWNLOAD_TIME))
                video.fileName = res.getString(FacebookDownloadEntity.INDEX_FILE_NAME)
                video.state = res.getInt(FacebookDownloadEntity.INDEX_STATE)
                video.fileUri = res.getString(FacebookDownloadEntity.INDEX_FILE_URI)
                video.downloadId = res.getLong(FacebookDownloadEntity.INDEX_DOWNLOAD_ID)
                video.outputPath = res.getString(FacebookDownloadEntity.INDEX_OUTPUT_PATH)
                video.videoSource = res.getInt(FacebookDownloadEntity.INDEX_VIDEO_SOURCE)
                array_list.add(video)
                res.moveToNext()
            }
            return array_list
        }

    fun getVideo(downloadId: Long): FVideo? {
        val db = this.readableDatabase
        val res = db.rawQuery(
            "select * from " + FacebookDownloadEntity.TABLE_NAME +
                    " where " + FacebookDownloadEntity.COLUMN_DOWNLOAD_ID + " = " + downloadId, null
        )
        if (res == null || res.count == 0 || res.columnCount == 0) return null
        res.moveToFirst()
        val video = FVideo(res.getLong(FacebookDownloadEntity.INDEX_DOWNLOAD_TIME))
        video.fileName = res.getString(FacebookDownloadEntity.INDEX_FILE_NAME)
        video.state = res.getInt(FacebookDownloadEntity.INDEX_STATE)
        video.fileUri = res.getString(FacebookDownloadEntity.INDEX_FILE_URI)
        video.isWatermarked = (res.getInt(FacebookDownloadEntity.INDEX_IS_WATERMARKED) > 0)
        video.downloadId = res.getLong(FacebookDownloadEntity.INDEX_DOWNLOAD_ID)
        video.outputPath = res.getString(FacebookDownloadEntity.INDEX_OUTPUT_PATH)
        video.videoSource = res.getInt(FacebookDownloadEntity.INDEX_VIDEO_SOURCE)
        return video
    }

    fun deleteVideo(downloadId: Long): Int {
        val db = this.writableDatabase
        return db.delete(
            FacebookDownloadEntity.TABLE_NAME,
            FacebookDownloadEntity.COLUMN_DOWNLOAD_ID + " = ? ",
            arrayOf(java.lang.Long.toString(downloadId))
        )
    }

    fun updateState(downloadId: Long, state: Int) {
        val db = this.writableDatabase
        db.execSQL(
            "UPDATE " + FacebookDownloadEntity.TABLE_NAME +
                    " SET " + FacebookDownloadEntity.COLUMN_STATE + " = " + state +
                    " WHERE " + FacebookDownloadEntity.COLUMN_DOWNLOAD_ID + " = " + downloadId
        )
        db.close()
    }

    fun setUri(downloadId: Long, uri: String) {
        val db = this.writableDatabase
        db.execSQL(
            "UPDATE " + FacebookDownloadEntity.TABLE_NAME +
                    " SET " + FacebookDownloadEntity.COLUMN_FILE_URI + " = '" + uri +
                    "' WHERE " + FacebookDownloadEntity.COLUMN_DOWNLOAD_ID + " = " + downloadId
        )
        db.close()
    }

    companion object {
        const val DATABASE_NAME = "weather.db"
        private const val DATABASE_VERSION = 5
    }
}