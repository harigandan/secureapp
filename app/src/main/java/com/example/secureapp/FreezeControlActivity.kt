package com.example.secureapp

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity

class FreezeControlActivity : ComponentActivity() {

    companion object {
        const val EXTRA_ACTION = "action"
        const val ACTION_FREEZE = "freeze"
        const val ACTION_UNFREEZE = "unfreeze"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make activity full screen and keep on top
        window.addFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        when (intent.getStringExtra(EXTRA_ACTION)) {
            ACTION_FREEZE -> {
                FreezeManager.showOverlay(this)

                // Start screen pinning / lock task
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (!isInLockTaskMode()) {
                        startLockTask()
                    }
                }
            }

            ACTION_UNFREEZE -> {
                FreezeManager.dismissOverlay()

                // Stop screen pinning
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    stopLockTask()
                }
            }
        }

        // Keep activity invisible in back stack
        finish()
    }

    private fun isInLockTaskMode(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val dpm = getSystemService(DEVICE_POLICY_SERVICE) as android.app.admin.DevicePolicyManager
            dpm.isLockTaskPermitted(packageName)
        } else false
    }
}
