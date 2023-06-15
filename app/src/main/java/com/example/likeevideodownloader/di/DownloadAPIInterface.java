package com.example.likeevideodownloader.di;


import com.example.likeevideodownloader.models.FacebookReel;
import com.example.likeevideodownloader.models.FacebookVideo;
import com.example.likeevideodownloader.models.InstaVideo;
import com.example.likeevideodownloader.models.LikeeVideo;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DownloadAPIInterface {
    @GET("/link.php")
    Call<FacebookVideo> getFacebookVideos(@Query("video") String videoUrl);

    @GET("/instagram.php")
    Call<InstaVideo> getInstaVideos(@Query("video") String videoUrl);

    @GET("/instagram.php")
    Call<LikeeVideo> getLikeeVideos(@Query("video") String videoUrl);

    @GET("/instagram.php")
    Call<FacebookReel> getFacebookReels(@Query("video") String videoUrl);
}
