package com.bluelock.likeevideodownloader.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bluelock.likeevideodownloader.adapter.MyAdapter
import com.bluelock.likeevideodownloader.databinding.ActivityDownloadedBinding
import com.bluelock.likeevideodownloader.interfaces.ItemClickListener
import com.bluelock.likeevideodownloader.remote.RemoteConfig
import com.bluelock.likeevideodownloader.util.Utils.RootDirectoryLikeeShow
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class DownloadedActivity : AppCompatActivity(), ItemClickListener {
    lateinit var binding: ActivityDownloadedBinding

    @Inject
    lateinit var googleManager: GoogleManager

    private var nativeAd: NativeAd? = null

    @Inject
    @GoogleAnalytics
    lateinit var analytics: Analytics

    @Inject
    lateinit var remoteConfig: RemoteConfig

    private var fileList: ArrayList<File> = ArrayList()
    private lateinit var myAdapter: MyAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRecyclerView()
        lifecycleScope.launch(Dispatchers.IO) { refreshFiles() }
        showNativeAd()
        showDropDown()
        initView()
    }

    private fun initView() {
        binding.apply {
            btnBack.setOnClickListener {
                showInterstitialAd {
                    finish()
                }
            }
        }
    }

    private fun initRecyclerView() {

        myAdapter = MyAdapter(fileList, this)

        binding.downloaded.apply {
            setHasFixedSize(true)
            layoutManager =
                LinearLayoutManager(
                    this@DownloadedActivity,
                    LinearLayoutManager.VERTICAL,
                    false
                )
            adapter = myAdapter
        }

    }

    private suspend fun refreshFiles() {
        fileList.clear()
        RootDirectoryLikeeShow.listFiles()?.let { f ->
            f.forEach {
                binding.empty.visibility = View.GONE
                fileList.add(it)
            }
        }
        withContext(Dispatchers.Main) {
            myAdapter.notifyDataSetChanged()
        }

    }


    override fun onItemClicked(file: File) {
        showInterstitialAd {
            val uri =
                FileProvider.getUriForFile(
                    this,
                    applicationContext.packageName + ".provider",
                    file
                );

            Intent().apply {
                action = Intent.ACTION_VIEW
                setDataAndType(uri, contentResolver.getType(uri))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(this)
            }
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
                showInterstitialAd {
                    binding.dropLayout.visibility = View.GONE
                }
            }
            binding.btnDropUp.visibility = View.INVISIBLE

        }
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

    override fun onDestroy() {
        super.onDestroy()
        showInterstitialAd { }
    }

}