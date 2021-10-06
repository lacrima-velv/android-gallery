package com.example.gallery.data

import android.net.Uri
import androidx.recyclerview.widget.DiffUtil
import java.util.*

//Data class to hold information about an image included in the device's MediaStore
data class MediaStoreImage(
    val id: Long,
    val dateAdded: Date,
    val contentUri: Uri
) {
    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<MediaStoreImage>() {
            override fun areItemsTheSame(
                oldItem: MediaStoreImage,
                newItem: MediaStoreImage
            ) = oldItem == newItem

            override fun areContentsTheSame(
                oldItem: MediaStoreImage,
                newItem: MediaStoreImage
            ) = oldItem.id == newItem.id
        }
    }
}