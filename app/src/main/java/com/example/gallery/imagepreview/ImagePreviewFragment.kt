package com.example.gallery.imagepreview

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.davemorrissey.labs.subscaleview.ImageSource
import com.example.gallery.data.ViewModel
import com.example.gallery.Utils.setUiWindowInsets
import com.example.gallery.databinding.FragmentImagePreviewBinding

class ImagePreviewFragment : Fragment() {

    private lateinit var binding: FragmentImagePreviewBinding
    private lateinit var viewModel: ViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewModel = ViewModelProvider(this).get(ViewModel::class.java)

        viewModel.isAppBarInPreviewVisible.observe(viewLifecycleOwner) { isVisible ->
            binding.appBarLayout.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
        }

        binding = FragmentImagePreviewBinding.inflate(inflater, container, false)
        // Get image URI from ImagesGridFragment to display it in scalable image view
        val imageUri = ImagePreviewFragmentArgs.fromBundle(requireArguments()).imageUri

        binding.imagePreview.setImage(ImageSource.uri(imageUri))

        // Remove/Add Top App Bar by tapping the screen
        binding.imagePreview.setOnClickListener {
            if (binding.appBarLayout.visibility == View.VISIBLE) {
                binding.appBarLayout.visibility = View.INVISIBLE
                viewModel.setAppBarInPreviewVisibility(false)
            } else if (binding.appBarLayout.visibility == View.INVISIBLE) {
                binding.appBarLayout.visibility = View.VISIBLE
                viewModel.setAppBarInPreviewVisibility(true)
            }
        }

        setUiWindowInsets(binding.root)

        val navController = findNavController()

        binding.topAppBar.setupWithNavController(navController)

        return binding.root
    }
}