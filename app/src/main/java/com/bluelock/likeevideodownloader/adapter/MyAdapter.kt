package com.bluelock.likeevideodownloader.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bluelock.likeevideodownloader.databinding.VideoItemBinding
import com.bluelock.likeevideodownloader.interfaces.ItemClickListener
import com.bumptech.glide.Glide
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class MyAdapter(private val fileList: ArrayList<File>, private val listener: ItemClickListener) :
    RecyclerView.Adapter<MyAdapter.ItemHolder>() {
    private companion object {
        const val format = "dd/MM/yyyy  hh:mm:ss"
    }

    inner class ItemHolder(private val v: VideoItemBinding) : RecyclerView.ViewHolder(v.root) {

        fun bind(p: Int) {
            Glide.with(v.videoImage.context).load(fileList[p]).into(v.videoImage)
            v.title.text = fileList[p].name
            Files.readAttributes(fileList[p].toPath(), BasicFileAttributes::class.java).apply {
                v.time.text = getDate(this.creationTime().toMillis())

            }
            v.root.setOnClickListener {
                listener.onItemClicked(fileList[p])
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder = ItemHolder(
        VideoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = fileList.size
    fun getDate(milliSeconds: Long): String? {
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat(format)

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }
}