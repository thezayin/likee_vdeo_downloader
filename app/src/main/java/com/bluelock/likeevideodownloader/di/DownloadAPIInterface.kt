package com.bluelock.likeevideodownloader.di

import com.bluelock.likeevideodownloader.models.LikeeVideo
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface DownloadAPIInterface {

    @GET("/instagram.php")
    fun getLikeeVideos(@Query("video") videoUrl: String?): Call<LikeeVideo?>?

}