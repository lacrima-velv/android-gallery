package com.example.gallery

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Resources
import android.os.Build
import android.util.TypedValue
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit


object Utils {
    /**
     * Make layout going from edge to edge if Android version is at least 11
     */
    fun Activity.makeEdgeToEdge() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
        } else {
            Timber.i("Cannot use makeFullscreen() as API Level < 30")
            return
        }
    }

    /**
     * Apply window insets to the view to avoid overlapping with system bars which occurs
     * if the app is displayed edge to edge
     */
    fun setUiWindowInsets(view: View, topPadding: Int = 0, bottomPadding: Int = 0) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            v.updatePadding(bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom + bottomPadding)
            v.updatePadding(top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top + topPadding)
            insets
        }
    }

    /**
     * Prevent multiple callback invocation. Threshold is hardcoded to 1000 ms.
     */
    fun View.setOnSingleClickListener(listener: View.OnClickListener) {
        var lastClickTime = 0L
        setOnClickListener { view ->
            if (System.currentTimeMillis() > lastClickTime + 1000) {
                listener.onClick(view)
                lastClickTime = System.currentTimeMillis()
            }
        }
    }

    /**
     * Convert dp to pixels
     */
    val Int.toPixels
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()

    /**
     * Convert a day/month/year date into a UNIX timestamp.
     *
     * We're suppressing the lint warning because we're not actually using the date formatter
     * to format the date to display, just to specify a format to use to parse it, and so the
     * locale warning doesn't apply.
     */
    @Suppress("SameParameterValue")
    @SuppressLint("SimpleDateFormat")
    fun dateToTimestamp(day: Int, month: Int, year: Int): Long =
        SimpleDateFormat("dd.MM.yyyy").let { formatter ->
            TimeUnit.MICROSECONDS.toSeconds(formatter.parse("$day.$month.$year")?.time ?: 0)
        }
}