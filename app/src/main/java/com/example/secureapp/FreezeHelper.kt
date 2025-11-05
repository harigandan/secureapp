package com.example.secureapp

import android.app.Activity
import android.util.Log
import android.widget.Toast

/**
 * This helper object is now DEPRECATED.
 *
 * The Kiosk Mode ("freeze") and "unfreeze" logic has been moved directly into MainActivity.kt.
 * This is the modern and correct approach, as it properly ties Kiosk Mode to the Activity's
 * lifecycle and centralizes control.
 *
 * This file is kept temporarily to avoid build errors from old code, but it should
 * no longer be used. All calls should be directed to MainActivity's internal methods.
 */
@Deprecated(
    "Kiosk Mode logic has been moved to MainActivity. This helper is no longer needed and should be removed.",
    ReplaceWith("Delete this file and use the implementation in MainActivity.")
)
object FreezeHelper {

    private const val TAG = "FreezeHelper-DEPRECATED"

    /**
     * This function is deprecated. The "freeze" action is now handled by sending an
     * intent to MainActivity, which then calls its internal startKioskMode() method.
     */
    fun freeze(activity: Activity) {
        Log.w(TAG, "freeze() called on deprecated FreezeHelper. Functionality moved to MainActivity.onResume().")
        Toast.makeText(activity, "FreezeHelper is deprecated. See logs.", Toast.LENGTH_LONG).show()
        // The actual freeze/kiosk mode logic now runs in MainActivity when it receives
        // the ACTION_START_KIOSK_MODE intent. No action is needed here.
    }

    /**
     * This function is deprecated. The "unfreeze" action should be handled by
     * MainActivity's own stopKioskMode() method.
     */
    fun unfreeze(activity: Activity) {
        Log.w(TAG, "unfreeze() called on deprecated FreezeHelper. Use the method in MainActivity instead.")
        Toast.makeText(activity, "FreezeHelper is deprecated. See logs.", Toast.LENGTH_LONG).show()

        // As a fallback, attempt to call the new method in MainActivity if possible.
        if (activity is MainActivity) {
            // =========================================================================
            //  THE FIX IS HERE: Call the correctly named function from MainActivity.
            // =========================================================================
            activity.stopKioskMode()
        } else {
            // Last resort for older code: directly try to stop the lock task.
            try {
                activity.stopLockTask()
                Log.d(TAG, "Attempted to stop Kiosk mode via fallback.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop LockTask via fallback: ${e.message}")
            }
        }
    }
}
