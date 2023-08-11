package com.bluelock.likeevideodownloader.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import com.bluelock.likeevideodownloader.R
import com.bluelock.likeevideodownloader.db.Database
import com.bluelock.likeevideodownloader.models.FVideo
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.File
import java.net.URLConnection

@Suppress("DEPRECATION")
class Utils {
    companion object {

        private const val LIKEE_REGEX = "^https?://l\\.likee\\.video/v/[a-zA-Z0-9]{6,}$"

        @SuppressLint("StaticFieldLeak")
        var customDialog: BottomSheetDialog? = null


        var RootDirectoryLikee = "/Smart_Downloader/likee/"


        var RootDirectoryLikeeShow = File(
            Environment.getExternalStorageDirectory().toString() + "/Download" + RootDirectoryLikee
        )


        fun setToast(mContext: Context?, str: String?) {
            val toast = Toast.makeText(mContext, str, Toast.LENGTH_SHORT)
            toast.show()
        }


        fun createLikeeFolder() {
            if (!RootDirectoryLikeeShow.exists()) {
                RootDirectoryLikeeShow.mkdirs()
            }
        }


        @SuppressLint("InflateParams")
        fun showProgressDialog(activity: Activity) {
            println("Show")
            if (customDialog != null) {
                customDialog!!.dismiss()
                customDialog = null
            }
            customDialog = BottomSheetDialog(activity, R.style.SheetDialog)
            val inflater = LayoutInflater.from(activity)
            val mView = inflater.inflate(R.layout.layout_progress_dialog, null)
            customDialog!!.setCancelable(false)
            customDialog!!.setContentView(mView)
            if (!customDialog!!.isShowing && !activity.isFinishing) {
                customDialog!!.show()
            }
        }

        fun hideProgressDialog() {
            println("Hide")
            if (customDialog != null && customDialog!!.isShowing) {
                customDialog!!.dismiss()
            }
        }


        fun startDownload(context: Context, videoUrl: String?): FVideo {
            setToast(context, context.resources.getString(R.string.download_started))
            val fileName: String = "likee_" + System.currentTimeMillis() + ".mp4"
            val downloadLocation: String = RootDirectoryLikee + fileName

            Log.d("jeje_url", (videoUrl)!!)
            val uri = Uri.parse(videoUrl) // Path where you want to download file.
            val request = DownloadManager.Request(uri)
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI) // Tell on which network you want to download file.
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE) // This will show notification on top when downloading the file.
            request.setTitle(fileName + "") // Title for notification.
            request.setVisibleInDownloadsUi(true)
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                downloadLocation
            ) // Storage directory path
            val downloadId =
                (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(
                    request
                ) // This will start downloading
            Log.d("jeje_req", request.toString())
            Log.d("jeje_downloadID", downloadId.toString())

            //Creating a video object to track download is completed
            val video = FVideo(
                Environment.getExternalStorageDirectory().toString() +
                        downloadLocation, fileName, downloadId, false, System.currentTimeMillis()
            )
            video.state = FVideo.DOWNLOADING
            video.videoSource = FVideo.LIKEE

            val db = Database.init(context)
            db.addVideo(video)
            Log.d(
                "jeje_startDownload: ",
                Environment.getDataDirectory().path + RootDirectoryLikee + fileName
            )
            try {
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(File(Environment.DIRECTORY_DOWNLOADS + "/" + fileName).absolutePath),
                    null
                ) { path, _ ->
                    Log.d(
                        "jeje_videoProcess",
                        "onScanCompleted: $path"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("jeje_e", e.toString())
            }
            val fVideo = FVideo(
                (Environment.getExternalStorageDirectory().toString() +
                        downloadLocation + fileName),
                fileName, downloadId, false, System.currentTimeMillis()
            )
            fVideo.videoSource = FVideo.FACEBOOK
            Log.d("jeje_set_Source", "fVideo.setVie")
            return fVideo
        }


        fun isLikeeUrl(url: String): Boolean {
            return url.matches(LIKEE_REGEX.toRegex())
        }


        @JvmStatic
        fun appInstalledOrNot(context: Context, packageName: String?): Boolean {
            val pm = context.packageManager
            return try {
                pm.getPackageInfo((packageName)!!, PackageManager.GET_ACTIVITIES)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }

        @JvmStatic
        fun deleteVideoFromFile(context: Context?, video: FVideo) {
            if (video.state == FVideo.COMPLETE) {
                val file = File(video.fileUri!!)
                val db = Database.init(context)
                if (file.exists()) {
                    AlertDialog.Builder(context)
                        .setTitle("Want to delete this file?")
                        .setMessage("This will delete file from your Disk")
                        .setPositiveButton("Yes") { _: DialogInterface?, _: Int ->
                            val isDeleted: Boolean = file.delete()
                            if (isDeleted) Toast.makeText(
                                context,
                                "Video deleted",
                                Toast.LENGTH_SHORT
                            ).show()
                            db.deleteAVideo(video.downloadId)
                        }
                        .setNegativeButton(
                            "No"
                        ) { dialog: DialogInterface, _: Int -> dialog.cancel() }
                        .show()
                } else {
                    db.deleteAVideo((video.downloadId))
                    Toast.makeText(context, "video not found", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "File downloading...", Toast.LENGTH_SHORT).show()
            }
        }


        fun isVideoFile(context: Context?, path: String): Boolean {
            return if (path.startsWith("content")) {
                val fromTreeUri = DocumentFile.fromSingleUri((context)!!, Uri.parse(path))
                val mimeType = fromTreeUri!!.type
                mimeType != null && mimeType.startsWith("video")
            } else {
                val mimeType = URLConnection.guessContentTypeFromName(path)
                mimeType != null && mimeType.startsWith("video")
            }
        }

    }
}