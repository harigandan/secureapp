package com.example.secureapp // Or your actual package name

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponentName: ComponentName
    private val TAG = "SecureAppMainActivity"

    companion object {
        // Action for the intent that will trigger Kiosk Mode
        const val ACTION_START_KIOSK_MODE = "com.example.secureapp.actions.START_KIOSK_MODE"
        const val ACTION_STOP_KIOSK_MODE = "com.example.secureapp.actions.STOP_KIOSK_MODE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponentName = ComponentName(this, MyDeviceAdminReceiver::class.java)

        // Whitelist the app for Lock Task Mode if it's the device owner.
        if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
            Log.i(TAG, "App is Device Owner. Setting lock task packages.")
            devicePolicyManager.setLockTaskPackages(adminComponentName, arrayOf(packageName))
        } else {
            Log.w(TAG, "App is NOT the device owner. Kiosk Mode will not work.")
        }

        ensurePermissions()

        setContent {
            val isDeviceOwner = devicePolicyManager.isDeviceOwnerApp(packageName)
            SecureAppScreen(
                isDeviceOwner = isDeviceOwner,
                onEnableAdmin = { enableDeviceAdmin() },
                onStopKioskMode = { stopKioskMode() }
            )
        }

        // Handle the intent that started the activity
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_START_KIOSK_MODE -> {
                Log.i(TAG, "Intent received to start Kiosk Mode.")
                startKioskMode()
            }
            ACTION_STOP_KIOSK_MODE -> {
                Log.i(TAG, "Intent received to stop Kiosk Mode.")
                stopKioskMode()
            }
        }
    }

    fun startKioskMode() {
        if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
            if (devicePolicyManager.isLockTaskPermitted(packageName)) {
                if (!isInLockTaskMode()) {
                    Log.i(TAG, "Starting Lock Task Mode now.")
                    startLockTask()
                }
            }
        } else {
            Log.e(TAG, "Cannot start Kiosk Mode: App is not the device owner.")
        }
    }

    fun stopKioskMode() {
        if (devicePolicyManager.isDeviceOwnerApp(packageName) && isInLockTaskMode()) {
            Log.i(TAG, "Stopping Lock Task Mode.")
            stopLockTask()
            Toast.makeText(this, "Kiosk Mode Deactivated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isInLockTaskMode(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        return activityManager.lockTaskModeState != android.app.ActivityManager.LOCK_TASK_MODE_NONE
    }

    // --- PERMISSION AND ADMIN METHODS ---
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { map ->
        if (map.values.any { !it }) {
            Toast.makeText(this, "Some permissions were denied.", Toast.LENGTH_LONG).show()
        }
    }

    private fun ensurePermissions() {
        val permissionsToRequest = listOfNotNull(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) Manifest.permission.ACCESS_BACKGROUND_LOCATION else null
        ).filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun enableDeviceAdmin() {
        // ... (enableDeviceAdmin logic as in previous versions)
    }
}

@Composable
fun SecureAppScreen(
    isDeviceOwner: Boolean,
    onEnableAdmin: () -> Unit,
    onStopKioskMode: () -> Unit
) {
    // ... (UI code as in previous versions)
}

