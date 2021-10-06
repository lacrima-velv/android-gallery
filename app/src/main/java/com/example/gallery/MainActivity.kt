package com.example.gallery

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.gallery.Utils.makeEdgeToEdge

import com.example.gallery.databinding.ActivityMainBinding
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        binding = ActivityMainBinding.inflate(layoutInflater)

        // Make layout going from edge to edge (only for API Level >= 30)
        this.makeEdgeToEdge()

        // Change from Splash screen theme to the default one
        setTheme(R.style.Theme_Gallery)

        setContentView(binding.root)
    }
}