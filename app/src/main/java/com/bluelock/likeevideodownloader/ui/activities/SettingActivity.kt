package com.bluelock.likeevideodownloader.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bluelock.likeevideodownloader.R
import com.bluelock.likeevideodownloader.databinding.ActivitySettingBinding
import com.bluelock.likeevideodownloader.remote.RemoteConfig
import com.example.ads.GoogleManager
import com.example.ads.databinding.MediumNativeAdLayoutBinding
import com.example.ads.databinding.NativeAdBannerLayoutBinding
import com.example.ads.newStrategy.types.GoogleInterstitialType
import com.example.ads.ui.binding.loadNativeAd
import com.example.analytics.dependencies.Analytics
import com.example.analytics.qualifiers.GoogleAnalytics
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding

    @Inject
    lateinit var googleManager: GoogleManager

    private var nativeAd: NativeAd? = null

    @Inject
    @GoogleAnalytics
    lateinit var analytics: Analytics

    @Inject
    lateinit var remoteConfig: RemoteConfig


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)


        observer()
        showNativeAd()
        if (remoteConfig.showDropDownAd) {
            showDropDown()
        }
    }

    private fun observer() {
        lifecycleScope.launch {
            binding.apply {
                btnBack.setOnClickListener {
                    showInterstitialAd {
                        finish()
                    }
                }

                lTerm.setOnClickListener {
                    showInterstitialAd {
                        intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://bluelocksolutions.blogspot.com/2023/06/likee-downloader-terms-and-conditions.html")
                        )
                        startActivity(intent)
                    }
                }
                lPrivacy.setOnClickListener {
                    showInterstitialAd {
                        intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://bluelocksolutions.blogspot.com/2023/06/privacy-policy-for-likee-downloader.html")
                        )
                        startActivity(intent)
                    }
                }
                lContact.setOnClickListener {
                    showInterstitialAd {
                        val emailIntent = Intent(
                            Intent.ACTION_SENDTO,
                            Uri.parse("mailto:blue.lock.testing@gmail.com")
                        )
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Likee Downloader")
                        emailIntent.putExtra(Intent.EXTRA_TEXT, "your message here")
                        startActivity(Intent.createChooser(emailIntent, "Chooser Title"))
                    }
                }
                lShare.setOnClickListener {
                    showInterstitialAd {
                        try {
                            val shareIntent = Intent(Intent.ACTION_SEND)
                            shareIntent.type = "text/plain"
                            shareIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name)
                            var shareMessage = "\nLet me recommend you this application\n\n"
                            shareMessage =
                                """
                            ${shareMessage + "https://play.google.com/store/apps/details?id=com.bluelock.likeevideodownloader"}
                            """.trimIndent()
                            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                            startActivity(Intent.createChooser(shareIntent, "choose one"))
                        } catch (e: java.lang.Exception) {
                            Log.d("jeje_e", e.toString())
                        }
                    }
                }
                lMore.setOnClickListener {
                    showInterstitialAd {
                        intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=7137279946828938614")
                        )
                        startActivity(intent)
                    }
                }
            }
        }
    }

    fun showNatAd(): Boolean {
        return remoteConfig.nativeAd
    }


    private fun showInterstitialAd(callback: () -> Unit) {
        if (remoteConfig.showInterstitial) {
            val ad: InterstitialAd? =
                googleManager.createInterstitialAd(GoogleInterstitialType.MEDIUM)

            if (ad == null) {
                callback.invoke()
                return
            } else {
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        callback.invoke()
                    }

                    override fun onAdFailedToShowFullScreenContent(error: AdError) {
                        super.onAdFailedToShowFullScreenContent(error)
                        callback.invoke()
                    }
                }
                ad.show(this)
            }
        } else {
            callback.invoke()
        }
    }


    private fun showNativeAd() {
        if (remoteConfig.nativeAd) {
            nativeAd = googleManager.createNativeAdSmall()
            nativeAd?.let {
                val nativeAdLayoutBinding = NativeAdBannerLayoutBinding.inflate(layoutInflater)
                nativeAdLayoutBinding.nativeAdView.loadNativeAd(ad = it)
                binding.nativeView.removeAllViews()
                binding.nativeView.addView(nativeAdLayoutBinding.root)
                binding.nativeView.visibility = View.VISIBLE
            }
        }
    }

    private fun showDropDown() {
        val nativeAdCheck = googleManager.createNativeFull()
        val nativeAd = googleManager.createNativeFull()
        Log.d("ggg_nul", "nativeAd:${nativeAdCheck}")

        nativeAdCheck?.let {
            Log.d("ggg_lest", "nativeAdEx:${nativeAd}")
            binding.apply {
                dropLayout.bringToFront()
                nativeViewDrop.bringToFront()
            }
            val nativeAdLayoutBinding = MediumNativeAdLayoutBinding.inflate(layoutInflater)
            nativeAdLayoutBinding.nativeAdView.loadNativeAd(ad = it)
            binding.nativeViewDrop.removeAllViews()
            binding.nativeViewDrop.addView(nativeAdLayoutBinding.root)
            binding.nativeViewDrop.visibility = View.VISIBLE
            binding.dropLayout.visibility = View.VISIBLE

            binding.btnDropDown.setOnClickListener {
                binding.dropLayout.visibility = View.GONE
            }
            binding.btnDropUp.visibility = View.INVISIBLE

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        showInterstitialAd { }
    }
}