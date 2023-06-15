package com.example.ads


import android.content.Context
import com.example.ads.newStrategy.GoogleAppOpen
import com.example.ads.newStrategy.GoogleBanner
import com.example.ads.newStrategy.GoogleInterstitial
import com.example.ads.newStrategy.GoogleNativeForLanguage
import com.example.ads.newStrategy.GoogleNativeForProcessing
import com.example.ads.newStrategy.GoogleNativeForSave
import com.example.ads.newStrategy.GoogleNativeFull
import com.example.ads.newStrategy.GoogleNativeHome
import com.example.ads.newStrategy.GoogleNativeSmall
import com.example.ads.newStrategy.GoogleRewarded
import com.example.ads.newStrategy.types.GoogleInterstitialType
import com.example.ads.newStrategy.types.GoogleRewardedType
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.rewarded.RewardedAd
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var googleNativeSmall: GoogleNativeSmall? = null
    private var googleInterstitial: GoogleInterstitial? = null
    private var googleAppOpen: GoogleAppOpen? = null
    private var googleNativeFull: GoogleNativeFull? = null
    private var googleRewarded: GoogleRewarded? = null
    private var googleNativeForLanguage: GoogleNativeForLanguage? = null
    private var googleNativeForProcessing: GoogleNativeForProcessing? = null
    private var googleNativeForSave: GoogleNativeForSave? = null
    private var googleNativeHome: GoogleNativeHome? = null

    private var googleBanner: GoogleBanner? = null


    private val testDeviceIds: List<String> = listOf(
        AdRequest.DEVICE_ID_EMULATOR,
        "2799362CE96AAA7736F188963E9E156C"
    )

    fun init() {


        MobileAds.initialize(context)

        if (BuildConfig.DEBUG)
            MobileAds.setRequestConfiguration(
                RequestConfiguration.Builder()
                    .setTestDeviceIds(testDeviceIds)
                    .build()
            )

        googleNativeSmall = GoogleNativeSmall(context);
        googleInterstitial = GoogleInterstitial(context);
        googleAppOpen = GoogleAppOpen(context);
        googleNativeFull = GoogleNativeFull(context)
        googleRewarded = GoogleRewarded(context)
        googleNativeForLanguage = GoogleNativeForLanguage(context)
        googleNativeForProcessing = GoogleNativeForProcessing(context)
        googleNativeForSave =
            GoogleNativeForSave(context)
        googleNativeHome = GoogleNativeHome(context)
        googleBanner = GoogleBanner(context)

    }

    fun createRewardedAd(type: GoogleRewardedType? = null): RewardedAd? {

        if (type == GoogleRewardedType.HIGH) {
            return googleRewarded?.getHighFloorAd(context);
        } else if (type == GoogleRewardedType.MEDIUM) {
            return googleRewarded?.getMediumAd(context);
        } else {
            return googleRewarded?.getDefaultAd(context);
        }
    }

    fun createInterstitialAd(type: GoogleInterstitialType): InterstitialAd? {


        return when (type) {
            GoogleInterstitialType.HIGH -> {
                googleInterstitial?.getHighFloorAd(context);
            }

            GoogleInterstitialType.MEDIUM -> {
                googleInterstitial?.getMediumAd(context);
            }

            else -> {
                googleInterstitial?.getDefaultAd(context);
            }
        }
    }

    fun createNativeFull(): NativeAd? {
        return googleNativeFull?.getDefaultAd(context);
    }

    fun createNativeAdSmall(): NativeAd? {
        return googleNativeSmall?.getDefaultAd(context);
    }

    fun createAppOpenAd(): AppOpenAd? {
        return googleAppOpen?.getAd(context);
    }

    fun createNativeAdForLanguage(): NativeAd? {
        return googleNativeForLanguage?.getDefaultAd(context)
    }

    fun createNativeAdForHome(): NativeAd? {
        return googleNativeHome?.getDefaultAd(context)
    }

    fun createNativeAdForProcessing(): NativeAd? {
        return googleNativeForProcessing?.getDefaultAd(context)
    }

    fun createNativeAdForSave(): NativeAd? {
        return googleNativeForSave?.getDefaultAd(context)
    }

    fun createBannerAd(): AdView? {
        return googleBanner?.getDefaultAd(context)
    }

}