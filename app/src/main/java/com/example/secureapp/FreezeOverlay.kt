package com.example.secureapp

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout

class FreezeOverlay(private val context: Context) {

    private val windowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var overlayView: FrameLayout? = null
    private var isShown: Boolean = false

    /**
     * Show overlay: dims screen & blocks touches
     */
    fun show() {
        if (isShown) return

        overlayView = FrameLayout(context).apply {
            setBackgroundColor(Color.parseColor("#80000000")) // Semi-transparent black
            isClickable = true
            isFocusable = true
            setOnTouchListener { _: View, _: MotionEvent -> true } // Block touches
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START

        try {
            windowManager.addView(overlayView, params)
            isShown = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Remove overlay
     */
    fun dismiss() {
        if (!isShown) return

        try {
            overlayView?.let { windowManager.removeView(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            overlayView = null
            isShown = false
        }
    }
}
