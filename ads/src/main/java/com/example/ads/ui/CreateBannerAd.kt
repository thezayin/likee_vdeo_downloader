package com.example.ads.ui

import android.content.Context
import com.example.ads.newStrategy.GoogleBanner
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

fun createBannerAd(context: Context, adUnitId: String = GoogleBanner.BANNER_ALL): AdView {
    return AdView(context).apply {
        this.adUnitId = adUnitId
        setAdSize(AdSize.BANNER)
        loadAd(AdRequest.Builder().build())
    }
}