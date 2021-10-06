package com.example.gallery.data

import android.app.Application
import android.content.ContentResolver
import android.content.ContentUris
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.gallery.Utils.dateToTimestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

class ViewModel(application: Application) : AndroidViewModel(application) {

    // List of images from device storage
    private val _images = MutableLiveData<List<MediaStoreImage>>()
    val images: LiveData<List<MediaStoreImage>>
        get() = _images

    // App bar in Image Preview changes visibility when user taps the screen
    private val _isAppBarInPreviewVisible = MutableLiveData<Boolean>()
    val isAppBarInPreviewVisible: LiveData<Boolean>
        get() = _isAppBarInPreviewVisible

    fun setAppBarInPreviewVisibility(isVisible: Boolean) {
        _isAppBarInPreviewVisible.value = isVisible
    }

    private var contentObserver: ContentObserver? = null

    fun loadImages() {
        Timber.d("loadImages() is called")
        viewModelScope.launch {
            val imageList = queryImages()
            _images.postValue(imageList)

            if (contentObserver == null) {
                contentObserver = getApplication<Application>().contentResolver.registerObserver(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ) {
                    loadImages()
                }
            }
        }
    }

    private suspend fun queryImages(): List<MediaStoreImage> {
        val images = mutableListOf<MediaStoreImage>()

        withContext(Dispatchers.IO) {
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_ADDED
            )

            val selection = "${MediaStore.Images.Media.DATE_ADDED} >= ?"
            /*
            Date when Android 5.0 was released (which corresponds to minSdkLevel).
            Choose images made since that date.
             */
            val selectionArgs = arrayOf(
                dateToTimestamp(day = 25, month = 6, year = 2014).toString()
            )

            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

            getApplication<Application>().contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dateModifiedColumn = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                Timber.d("Found ${cursor.count} images")
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val dateModified = Date(
                        TimeUnit.SECONDS.toMillis(
                            cursor.getLong(dateModifiedColumn)
                        )
                    )
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    val image = MediaStoreImage(id, dateModified, contentUri)
                    images += image
                }
            }
        }
        Timber.d("Found ${images.size} images")

        return images
    }

    override fun onCleared() {
        contentObserver?.let {
            getApplication<Application>().contentResolver.unregisterContentObserver(it)
        }
    }

    private fun ContentResolver.registerObserver(
        uri: Uri,
        observer: (selfChange: Boolean) -> Unit
    ): ContentObserver {
        val contentObserver = object : ContentObserver(Handler()) {
            override fun onChange(selfChange: Boolean) {
                observer(selfChange)
            }
        }
        registerContentObserver(uri, true, contentObserver)
        return contentObserver
    }

}

