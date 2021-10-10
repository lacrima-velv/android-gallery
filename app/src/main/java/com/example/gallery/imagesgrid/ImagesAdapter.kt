package com.example.gallery.imagesgrid

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import coil.size.ViewSizeResolver
import com.example.gallery.data.MediaStoreImage
import com.example.gallery.R
import com.example.gallery.Utils.setOnSingleClickListener
import com.example.gallery.databinding.PictureElementBinding

class ImagesAdapter(
    private val onClick: (mediaStoreImage: MediaStoreImage) -> Unit,
    private val onLongClick: (mediaStoreImage: MediaStoreImage) -> Unit
) :
    ListAdapter<MediaStoreImage, ImagesAdapter.ImageItemViewHolder>(MediaStoreImage.DiffCallback) {

    inner class ImageItemViewHolder(binding: PictureElementBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val imageView = binding.picture
        private val viewSize = ViewSizeResolver(imageView)
        private val imageLoader = imageView.context.imageLoader

        // Show Toast, when image is corrupted inside image view
        val onErrorClickListener = {
            imageView.setOnSingleClickListener {
                Toast.makeText(imageView.context,
                    imageView.context.getString(R.string.toast_image_is_corrupted),
                    Toast.LENGTH_SHORT)
                    .show()
            }
        }

        /*
        Open image in system Gallery when it's tapped. Open image in Image Preview when it's
        long tapped.
         */
        val onSuccessClickListener: (MediaStoreImage) -> Unit = { mediaStoreImage ->
            imageView.setOnClickListener {
                imageView.apply {
                    setOnLongClickListener {
                        onLongClick(mediaStoreImage)
                        true
                    }
                    setOnSingleClickListener {
                        onClick(mediaStoreImage)
                    }
                }
            }
        }

        private val preRequest = ImageRequest.Builder(imageView.context)
            .target(imageView)
            .crossfade(true)
            .placeholder(R.drawable.baseline_image)
            .error(R.drawable.broken_image)
            .scale(Scale.FIT)
            .size(viewSize)
            .precision(Precision.EXACT)

        private fun createRequest(
            preRequest: ImageRequest.Builder,
            mediaStoreImage: MediaStoreImage
        ) = preRequest
            .data(mediaStoreImage.contentUri)
            .listener(
                onError = { _, _ ->
                    onErrorClickListener()
                },
                onSuccess = { _, _ ->
                    onSuccessClickListener(mediaStoreImage)
                }
            )
            .build()

        fun bind(mediaStoreImage: MediaStoreImage) {
            val request = createRequest(preRequest, mediaStoreImage)
            imageLoader.enqueue(request)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageItemViewHolder {
        return ImageItemViewHolder(
            PictureElementBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ImageItemViewHolder, position: Int) {
        val mediaStoreImage = getItem(position)
        holder.bind(mediaStoreImage)
    }

}