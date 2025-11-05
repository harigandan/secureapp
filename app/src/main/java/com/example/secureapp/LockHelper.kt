package com.example.secureapp

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.util.Log

object LockHelper {
    fun lockNow(context: Context) {
        try {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val compName = ComponentName(context, MyDeviceAdminReceiver::class.java)
            if (dpm.isAdminActive(compName)) {
                dpm.lockNow()  // Only lock the device
                // goToSleep() <-- remove this line, causes Unresolved reference
                Log.d("SecureDroid", "Device locked successfully")
            } else {
                Log.e("SecureDroid", "Device Admin not active")
            }
        } catch (e: Exception) {
            Log.e("SecureDroid", "Lock failed: ${e.message}")
        }
    }
}

