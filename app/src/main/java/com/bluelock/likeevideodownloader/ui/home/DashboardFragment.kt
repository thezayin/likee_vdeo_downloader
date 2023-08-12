package com.bluelock.likeevideodownloader.ui.home

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bluelock.likeevideodownloader.R
import com.bluelock.likeevideodownloader.databinding.FragmentDashboardBinding
import com.bluelock.likeevideodownloader.db.Database
import com.bluelock.likeevideodownloader.di.ApiClient
import com.bluelock.likeevideodownloader.di.DownloadAPIInterface
import com.bluelock.likeevideodownloader.models.FVideo
import com.bluelock.likeevideodownloader.models.LikeeVideo
import com.bluelock.likeevideodownloader.models.ListAdapter
import com.bluelock.likeevideodownloader.remote.RemoteConfig
import com.bluelock.likeevideodownloader.ui.base.BaseFragment
import com.bluelock.likeevideodownloader.util.Constants
import com.bluelock.likeevideodownloader.util.Constants.downloadVideos
import com.bluelock.likeevideodownloader.util.Utils
import com.bluelock.likeevideodownloader.util.Utils.Companion.startDownload
import com.bluelock.likeevideodownloader.util.isConnected
import com.example.ads.GoogleManager
import com.example.ads.databinding.MediumNativeAdLayoutBinding
import com.example.ads.databinding.NativeAdBannerLayoutBinding
import com.example.ads.newStrategy.types.GoogleInterstitialType
import com.example.ads.ui.binding.loadNativeAd
import com.example.analytics.dependencies.Analytics
import com.example.analytics.events.AnalyticsEvent
import com.example.analytics.qualifiers.GoogleAnalytics
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import javax.inject.Inject


@Suppress("DEPRECATION")
@AndroidEntryPoint
class DashboardFragment : BaseFragment<FragmentDashboardBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentDashboardBinding =
        FragmentDashboardBinding::inflate

    @Inject
    lateinit var googleManager: GoogleManager

    @Inject
    @GoogleAnalytics
    lateinit var analytics: Analytics

    @Inject
    lateinit var remoteConfig: RemoteConfig

    private var nativeAd: NativeAd? = null


    private var urlType = 0

    private var adapter: ListAdapter? = null
    var videos: ArrayList<FVideo>? = null
    var db: Database? = null
    private var downloadAPIInterface: DownloadAPIInterface? = null
    private var activity: Activity? = null
    private var onCreateIsCalled = false
    lateinit var downloadingDialog: BottomSheetDialog

    private lateinit var consentInformation: ConsentInformation
    private lateinit var csForm: ConsentForm


    override fun onCreatedView() {
        showDownloadingDialog()
        showDropDown()
        showNativeAd()
        activity = requireActivity()
        onCreateIsCalled = true
        checkPermission()
        downloadAPIInterface = ApiClient.getInstance(
            resources.getString(R.string.download_api_base_url)
        ).create(DownloadAPIInterface::class.java)


        db = Database.init(requireActivity())
        db?.setCallback {
            updateListData()
        }

        requireActivity().registerReceiver(
            downloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )

        showConsent()

        showDropDown()

        initViews()
        handleIntent()
        observe()
        showRecursiveAds()
        onClick()

    }

    private fun showDownloadingDialog() {
        downloadingDialog = BottomSheetDialog(requireActivity(), R.style.SheetDialog)
        downloadingDialog.setContentView(R.layout.dialog_downloading)
        val adView = downloadingDialog.findViewById<FrameLayout>(R.id.nativeViewAdDownload)
        if (remoteConfig.nativeAd) {
            nativeAd = googleManager.createNativeAdSmall()
            nativeAd?.let {
                val nativeAdLayoutBinding = NativeAdBannerLayoutBinding.inflate(layoutInflater)
                nativeAdLayoutBinding.nativeAdView.loadNativeAd(ad = it)
                adView?.removeAllViews()
                adView?.addView(nativeAdLayoutBinding.root)
                adView?.visibility = View.VISIBLE
            }
        }

        downloadingDialog.behavior.isDraggable = false
//        downloadingDialog.setCanceledOnTouchOutside(false)


    }

    private fun initViews() {
        binding.apply {
            askReadPermission()
            askWritePermission()
            adapter = ListAdapter(
                requireActivity()
            ) { video ->
                when (video.state) {
                    FVideo.DOWNLOADING ->                         //video is in download state
                        Toast.makeText(
                            requireActivity(), "Video Downloading", Toast.LENGTH_LONG
                        ).show()

                    FVideo.PROCESSING ->                         //Video is processing
                        Toast.makeText(
                            requireActivity(), "Video Processing", Toast.LENGTH_LONG
                        ).show()

                    FVideo.COMPLETE -> {
                        //complete download and processing ready to use
                        val location: String = video.fileUri!!


                        //Downloaded video play into video player
                        val file = File(location)
                        if (file.exists()) {
                            val uri = Uri.parse(location)
                            val intent1 = Intent(Intent.ACTION_VIEW)
                            if (Utils.isVideoFile(
                                    requireActivity(), video.fileUri!!
                                )
                            ) {
                                intent1.setDataAndType(
                                    uri, "video/*"
                                )
                            } else intent1.setDataAndType(uri, "image/*")
                            if (intent1.resolveActivity(requireActivity().packageManager) != null) startActivity(
                                intent1
                            ) else Toast.makeText(
                                requireActivity(),
                                "No application can view this",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {

                            //File doesn't exists
                            Toast.makeText(
                                requireActivity(), "File doesn't exists", Toast.LENGTH_LONG
                            ).show()
                            //Delete the video instance from the list
                            db?.deleteAVideo(video.downloadId)
                        }
                    }

                    else -> {}
                }
            }

            recyclerView.layoutManager = LinearLayoutManager(requireActivity())
            updateListData()
            recyclerView.adapter = adapter
            ItemTouchHelper(object :
                ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val adapterPosition = viewHolder.adapterPosition
                    db?.deleteAVideo(videos!![adapterPosition].downloadId)
                }
            }).attachToRecyclerView(recyclerView)

        }
    }

    private fun observe() {
        binding.apply {
            btnSetting.setOnClickListener {
                showRewardedAd {}
                val action = DashboardFragmentDirections.actionDashboardFragmentToSettingFragment()
                findNavController().navigate(action)
            }

            linkEt.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (s.toString().trim { it <= ' ' }.isEmpty()) {
                        btnDownload.isEnabled = false
                        ivCross.visibility = View.GONE

                    } else {
                        btnDownload.isEnabled = true
                        ivCross.visibility = View.VISIBLE
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence, start: Int, count: Int, after: Int
                ) {
                    Log.d("jejeText", "before")
                }

                override fun afterTextChanged(s: Editable) {
                    Log.d("jejeYes", "after")
                }
            })

            ivCross.setOnClickListener {
                showRewardedAd {}
                linkEt.text = null
            }

            btnDownload.setOnClickListener {
                val ll = linkEt.text.toString().trim { it <= ' ' }
                analytics.logEvent(
                    AnalyticsEvent.LINK(
                        status = ll
                    )
                )
                if (ll == "") {
                    Utils.setToast(requireActivity(), resources.getString(R.string.enter_url))
                } else if (!Patterns.WEB_URL.matcher(ll).matches()) {
                    Utils.setToast(
                        requireActivity(), resources.getString(R.string.enter_valid_url)
                    )
                } else {
                    //Recheck url type if it previously no checked
                    if (urlType == 0) {
                        urlType = Constants.LIKEE_url

                    }
                    when (urlType) {
                        Constants.LIKEE_url -> {
                            getLikeData()
                        }
                    }

                }
            }
        }
    }


    private fun showConsent() {
        val params = ConsentRequestParameters.Builder().setTagForUnderAgeOfConsent(false).build()

        consentInformation = UserMessagingPlatform.getConsentInformation(requireActivity())
        consentInformation.requestConsentInfoUpdate(requireActivity(), params, {
            if (consentInformation.isConsentFormAvailable) {
                loadForm()
            }
        }, {
            // Handle the error.
        })
    }

    private fun loadForm() {
        UserMessagingPlatform.loadConsentForm(requireActivity(), {
            csForm = it
            if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                csForm.show(
                    requireActivity()
                ) {
                    if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.OBTAINED) {
//nothing here
                    }
                    loadForm()
                }
            }
        }, {
            // Handle the error.
        })
    }


    //Ads Views
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

    private fun showInterstitialAd(callback: () -> Unit) {

        val ad: InterstitialAd? = googleManager.createInterstitialAd(GoogleInterstitialType.MEDIUM)

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
            ad.show(requireActivity())
        }

    }

    private fun showDropDown() {
        val nativeAdCheck = googleManager.createNativeFull()
        val nativeAd = googleManager.createNativeFull()
        nativeAdCheck?.let {
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

    private fun onClick() {
        binding.apply {
            btnDownloaded.setOnClickListener {
                showRewardedAd {}
                val action =
                    DashboardFragmentDirections.actionDashboardFragmentToDownloadedFragment()
                findNavController().navigate(action)
            }

            btnDownload.setOnClickListener {
                val ll = linkEt.text.toString().trim { it <= ' ' }
                if (ll == "") {
                    Utils.setToast(
                        requireActivity(), resources.getString(R.string.enter_url)
                    )
                } else if (!Patterns.WEB_URL.matcher(ll).matches()) {
                    Utils.setToast(
                        requireActivity(), resources.getString(R.string.enter_valid_url)
                    )
                } else {
                    if (urlType == 0) {
                        urlType = Constants.LIKEE_url
                    }
                    when (urlType) {

                        Constants.LIKEE_url -> {
                            getLikeData()
                        }
                    }

                }
            }
        }
    }


    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionX.init(this).permissions(Manifest.permission.POST_NOTIFICATIONS)
                .onExplainRequestReason { scope, deniedList ->
                    scope.showRequestReasonDialog(
                        deniedList,
                        "Core fundamental are based on these permissions",
                        "OK",
                        "Cancel"
                    )
                }.onForwardToSettings { scope, deniedList ->
                    scope.showForwardToSettingsDialog(
                        deniedList,
                        "You need to allow necessary permissions in Settings manually",
                        "OK",
                        "Cancel"
                    )
                }.request { allGranted, _, _ ->
                    if (allGranted) {
                        Log.d("jeje_Ok", "All permissions are granted")
                    } else {
                        Toast.makeText(
                            requireActivity(),
                            "These permissions are denied: \$deniedList",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            PermissionX.init(this).permissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList, "Core fundamental are based on these permissions", "OK", "Cancel"
                )
            }.onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    "You need to allow necessary permissions in Settings manually",
                    "OK",
                    "Cancel"
                )
            }.request { _, _, _ -> /*if (allGranted) {
                                        Toast.makeText(MainActivity.this, "All permissions are granted", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(MainActivity.this, "These permissions are denied: $deniedList", Toast.LENGTH_LONG).show();
                                    }*/
            }
        }
    }


    private fun getLikeData() {
        binding.apply {
            Utils.createLikeeFolder()

            Utils.showProgressDialog(requireActivity())
            val videoLink: String = linkEt.text.toString().trim { it <= ' ' }
            val video = downloadAPIInterface!!.getLikeeVideos(videoLink)
            video?.enqueue(object : Callback<LikeeVideo?> {
                override fun onResponse(call: Call<LikeeVideo?>, response: Response<LikeeVideo?>) {
                    Utils.hideProgressDialog()
                    if (response.isSuccessful) {
                        val likeeVideo = response.body()
                        if (likeeVideo == null) {
                            showStartDownloadDialogR("")
                            return
                        }
                        if (!likeeVideo.error) {
                            val data = likeeVideo.data
                            if (data?.size == 1) {
                                showStartDownloadDialogR(data[0].url)
                            } else if (data?.size!! > 1) {
                                for (i in data.indices) {
                                    if (data[i].format_id.equals("mp4-without-watermark")) showStartDownloadDialogR(
                                        data[i].url
                                    )
                                }
                            }
                        } else {
                            showStartDownloadDialogR("")
                        }
                    }
                }

                override fun onFailure(call: Call<LikeeVideo?>, t: Throwable) {
                    Utils.hideProgressDialog()
                    showStartDownloadDialogR("")
                }
            })
        }
    }


    private fun updateListData() {
        binding.apply {
            videos = db?.recentVideos
        }
    }


    private fun handleIntent() {
        val intent = requireActivity().intent

        if (intent == null || intent.action == null) {
            return
        }
        if (intent.action == Intent.ACTION_SEND && intent.type != null) {
            if (intent.type == "text/plain") {
                // Extract the shared video URL from the intent's extras bundle
                val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return
                onCreateIsCalled = false
            }
        }
    }

    private val downloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            downloadingDialog.dismiss()
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadVideos.containsKey(id)) {
                val fVideo: FVideo? = db?.getVideo(id)
                val videoPath: String = Environment.getExternalStorageDirectory()
                    .toString() + "/Download" + Utils.RootDirectoryLikee + fVideo?.fileName

                val dialog = BottomSheetDialog(requireActivity(), R.style.SheetDialog)
                dialog.setContentView(R.layout.dialog_download_success)
                val btnOk = dialog.findViewById<Button>(R.id.btn_clear)
                val btnClose = dialog.findViewById<ImageView>(R.id.ivCross)
                val adView = dialog.findViewById<FrameLayout>(R.id.nativeViewAdSuccess)
                dialog.behavior.isDraggable = false
                dialog.setCanceledOnTouchOutside(false)
                if (showNatAd()) {
                    nativeAd = googleManager.createNativeAdSmall()
                    nativeAd?.let {
                        val nativeAdLayoutBinding =
                            NativeAdBannerLayoutBinding.inflate(layoutInflater)
                        nativeAdLayoutBinding.nativeAdView.loadNativeAd(ad = it)
                        adView?.removeAllViews()
                        adView?.addView(nativeAdLayoutBinding.root)
                        adView?.visibility = View.VISIBLE
                    }
                }

                btnOk?.setOnClickListener {
                    showRewardedAd {}
                    dialog.dismiss()

                }
                btnClose?.setOnClickListener {
                    showRewardedAd {}
                    dialog.dismiss()
                }

                dialog.show()
                db?.updateState(id, FVideo.COMPLETE)
                db?.setUri(id, videoPath)
            }
        }
    }

    private fun showStartDownloadDialogR(link: String?) {
        try {
            if (link == null || link == "") {
                val dialog = BottomSheetDialog(requireActivity(), R.style.SheetDialog)
                dialog.setContentView(R.layout.dialog_bottom_video_not_found_)
                val btnOk = dialog.findViewById<Button>(R.id.btn_clear)
                val btnCancel = dialog.findViewById<ImageView>(R.id.ivCross)
                val adView = dialog.findViewById<FrameLayout>(R.id.nativeViewNot)
                if (remoteConfig.nativeAd) {
                    nativeAd = googleManager.createNativeAdSmall()
                    nativeAd?.let {
                        val nativeAdLayoutBinding =
                            NativeAdBannerLayoutBinding.inflate(layoutInflater)
                        nativeAdLayoutBinding.nativeAdView.loadNativeAd(ad = it)
                        adView?.removeAllViews()
                        adView?.addView(nativeAdLayoutBinding.root)
                        adView?.visibility = View.VISIBLE
                    }
                }

                dialog.behavior.isDraggable = false
                dialog.setCanceledOnTouchOutside(false)

                btnOk?.setOnClickListener {
                    showRewardedAd {}
                    dialog.dismiss()

                }
                btnCancel?.setOnClickListener {
                    showRewardedAd { }
                    dialog.dismiss()
                }
                dialog.show()
                return
            }
            lifecycleScope.launch {

                val dialog = BottomSheetDialog(requireActivity(), R.style.SheetDialog)
                dialog.setContentView(R.layout.dialog_bottom_start_download)
                val btnDownload = dialog.findViewById<Button>(R.id.btn_download)
                val btnCancel = dialog.findViewById<ImageView>(R.id.ivCross)
                val adView = dialog.findViewById<FrameLayout>(R.id.nativeViewAdDownload)
                if (remoteConfig.nativeAd) {
                    nativeAd = googleManager.createNativeAdSmall()
                    nativeAd?.let {
                        val nativeAdLayoutBinding =
                            NativeAdBannerLayoutBinding.inflate(layoutInflater)
                        nativeAdLayoutBinding.nativeAdView.loadNativeAd(ad = it)
                        adView?.removeAllViews()
                        adView?.addView(nativeAdLayoutBinding.root)
                        adView?.visibility = View.VISIBLE
                    }
                }

                dialog.behavior.isDraggable = false
                dialog.setCanceledOnTouchOutside(false)
                btnDownload?.setOnClickListener {
                    showRewardedAd { }
                    videoDownloadR(link)
                    dialog.dismiss()
                    downloadingDialog.show()

                }
                btnCancel?.setOnClickListener {
                    showRewardedAd { }
                    dialog.dismiss()
                }
                dialog.show()
            }

        } catch (e: NullPointerException) {
            e.printStackTrace()
            Toast.makeText(requireActivity(), "Video Not Found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun askWritePermission() {
        val result = ContextCompat.checkSelfPermission(
            requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (Build.VERSION.SDK_INT < 32 && result != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1
            )
        }
    }

    private fun askReadPermission() {
        val result = ContextCompat.checkSelfPermission(
            requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (Build.VERSION.SDK_INT < 32 && result != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1
            )
        }
    }

    private fun videoDownloadR(videoUrl: String?) {
        binding.apply {
            if (videoUrl == null || videoUrl == "") {
                Toast.makeText(activity, "This video quality is not available", Toast.LENGTH_SHORT)
                    .show()
                return
            }
            val fVideo: FVideo = startDownload(requireActivity(), videoUrl)
            downloadVideos[fVideo.downloadId] = fVideo
            linkEt.setText("")
        }

    }

    private fun showRewardedAd(callback: () -> Unit) {
        if (remoteConfig.showInterstitial) {
            if (!requireActivity().isConnected()) {
                callback.invoke()
                return
            }
            val ad: RewardedAd? = googleManager.createRewardedAd()

            if (ad == null) {
                callback.invoke()
            } else {
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {

                    override fun onAdFailedToShowFullScreenContent(error: AdError) {
                        super.onAdFailedToShowFullScreenContent(error)
                        callback.invoke()
                    }
                }

                ad.show(requireActivity()) {
                    callback.invoke()
                }
            }
        } else {
            callback.invoke()
        }
    }

    private fun showRecursiveAds() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (this.isActive) {
                    showNativeAd()
                    if (remoteConfig.nativeAd) {
                        showNativeAd()
                    }
                    delay(1000L)
                }
            }
        }
    }

    fun showNatAd(): Boolean {
        return remoteConfig.nativeAd
    }
}
