package com.example.secureapp

import android.content.Context

object FreezeManager {
    private var _currentOverlay: FreezeOverlay? = null

    val isFrozen: Boolean
        get() = _currentOverlay != null

    fun showOverlay(context: Context) {
        if (_currentOverlay == null) {
            _currentOverlay = FreezeOverlay(context)
            _currentOverlay?.show()
        }
    }

    fun dismissOverlay() {
        _currentOverlay?.dismiss()
        _currentOverlay = null
    }
}
