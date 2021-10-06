package com.example.gallery.imagesgrid

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gallery.R
import com.example.gallery.data.MediaStoreImage
import com.example.gallery.Utils.setUiWindowInsets
import com.example.gallery.Utils.toPixels
import com.example.gallery.data.ViewModel
import com.example.gallery.databinding.FragmentImagesGridBinding
import com.example.gallery.permissiondialogs.DeniedPermissionDialogExplanation
import com.example.gallery.permissiondialogs.DeniedPermissionDialogShowRationaleFragment
import timber.log.Timber

const val SPAN_COUNT_PORTRAIT = 4
const val SPAN_COUNT_LANDSCAPE = 2

/*
This fragment must implement an interface to set onClickListener to a button in
Permission Rationale Dialog
 */
class ImagesGridFragment : Fragment(),
    DeniedPermissionDialogShowRationaleFragment.DialogClickListener {

    private lateinit var binding: FragmentImagesGridBinding
    private lateinit var viewModel: ViewModel
    private lateinit var activityResultLauncherRequestPermission: ActivityResultLauncher<String>
    private lateinit var imagesAdapter: ImagesAdapter
    private val topPaddingDp = 4
    private val bottomPaddingDp = 4

    /*
    Override a method from interface used in dialog for permission rationale
    to request permission to storage by clicking Yes button
    */
    override fun onYesClick() {
        requestPermission()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*
        Register the permissions callback, which handles the user's response to the
        system permissions dialog. Save the return value, an instance of
        ActivityResultLauncher.
         */
        activityResultLauncherRequestPermission = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                // Permission is granted. Continue the workflow
                showImages()
            } else {
                /*
                Explain to the user that the feature is unavailable because the
                features requires a permission that the user has denied. At the
                same time, respect the user's decision.
                 */
                showExplanationOfPermissionRequiringNoOptions()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImagesGridBinding.inflate(inflater, container, false)

        // Set Adapter for RecyclerView
        imagesAdapter = ImagesAdapter(imageItemOnClickListener, imageItemOnLongClickListener)

        binding.galleryGrid.adapter = imagesAdapter

        imagesAdapter.registerAdapterDataObserver(
            observeGrid()
        )

        val screenOrientation = this.resources.configuration.orientation
        // Change columns' quantity when orientation changes
        if (screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
            binding.galleryGrid.layoutManager =
                GridLayoutManager(requireActivity(), SPAN_COUNT_PORTRAIT,
                    GridLayoutManager.HORIZONTAL, false)
        } else {
            binding.galleryGrid.layoutManager =
                GridLayoutManager(requireActivity(), SPAN_COUNT_LANDSCAPE,
                    GridLayoutManager.HORIZONTAL, false)
        }

        viewModel = ViewModelProvider(this).get(ViewModel::class.java)

        // When images on device are changed, add/remove them in RecyclerView
        viewModel.images.observe(viewLifecycleOwner) { images ->
            imagesAdapter.submitList(images)
        }

        // Correct top and bottom padding, because they are not respected when setting window insets
        val topPaddingPx = topPaddingDp.toPixels
        val bottomPaddingPx = bottomPaddingDp.toPixels

        setUiWindowInsets(binding.galleryGrid, topPaddingPx, bottomPaddingPx)

        Timber.d("openMediaStore() is calling in onCreateView()")
        openMediaStore()

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        /*
        If the permission is already granted, but placeholder for this case is still visible,
        probably the user has just granted the permission to storage in system settings,
        which previously was rejected.
         */
        if (
            checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED &&
            binding.imagePermission.visibility == View.VISIBLE) {
            Timber.d("showImages() is calling inside onStart()")
            showImages()
        }
    }

    // Set onClickListener to image item. System image gallery must be opened.
    private val imageItemOnClickListener: (MediaStoreImage) -> Unit = { mediaStoreImage ->
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            type = "image/*"
            data = mediaStoreImage.contentUri
        }
        try {
            startActivity(intent)
        } catch (exception: ActivityNotFoundException) {
            Toast.makeText(context,
                getString(R.string.toast_no_system_galleries),
                Toast.LENGTH_LONG)
                .show()
        }
    }

    // Set onLongClickListener to image item. Image Preview must be shown.
    private val imageItemOnLongClickListener: (MediaStoreImage) -> Unit = { mediaStoreImage ->
        val imageUri = mediaStoreImage.contentUri.toString()
        findNavController().navigate(ImagesGridFragmentDirections
            .actionImagesGridFragmentToImagePreviewFragment(imageUri))
    }

    private fun requestPermission() {
        /*
        After launch() is called, the system permissions dialog appears.
        When the user makes a choice, the system asynchronously invokes your
        implementation of ActivityResultCallback
        */
        Timber.d("Permission to storage is requested")
        activityResultLauncherRequestPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    // Call when app is ready to load and display images
    private fun showImages() {
        Timber.d("showImages() is called")
        binding.imagePermission.visibility = View.GONE
        binding.textPermission.visibility = View.GONE
        viewModel.loadImages()
    }

    // Show placeholder when permission to storage is not granted
    private fun showNoAccess() {
        binding.imagePermission.visibility = View.VISIBLE
        binding.textPermission.visibility = View.VISIBLE
    }

    // Show dialog with rationale and and a button to request the permission again
    private fun showDeniedPermissionShowRationale() {
        val dialog = DeniedPermissionDialogShowRationaleFragment()
        dialog.show(childFragmentManager, "Rationale")
    }
    /*
    Show dialog with explanation why the permission is necessary,
    but don't provide an option to request it again
     */
    private fun showExplanationOfPermissionRequiringNoOptions() {
        val dialog = DeniedPermissionDialogExplanation()
        dialog.show(childFragmentManager, "Explanation")
    }

    // General function for loading images and requesting permission if it's necessary
    private fun openMediaStore() {
        when (checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            PackageManager.PERMISSION_GRANTED ->
                // Use the API that requires the permission.
            {
                Timber.d("Permission is granted")
                showImages()
            }
            PackageManager.PERMISSION_DENIED ->
            { Timber.d("Permission is denied")
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showNoAccess()
                    Timber.d("Permission rationale is requested")
                    showDeniedPermissionShowRationale()
                } else {
                    Timber.d("Permission rationale is not requested")
                    showNoAccess()
                    requestPermission()
                }
            }
        }
    }

    /*
    Set ListObserver for RecyclerView.Adapter. If the list is empty, the appropriate placeholder
    must be displayed
    */
    private fun observeGrid() = object : RecyclerView.AdapterDataObserver() {

        override fun onChanged() {
            super.onChanged()
            checkEmpty()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            checkEmpty()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            super.onItemRangeRemoved(positionStart, itemCount)
            checkEmpty()
        }

        fun checkEmpty() {
            Timber.d("checkEmpty() is called")
            if (binding.imagePermission.visibility == View.GONE && imagesAdapter.itemCount == 0) {
                binding.imageEmpty.visibility = View.VISIBLE
                binding.textEmpty.visibility = View.VISIBLE
            } else {
                binding.imageEmpty.visibility = View.GONE
                binding.textEmpty.visibility = View.GONE
            }
        }
    }

}