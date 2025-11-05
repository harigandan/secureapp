package com.example.secureapp // Your package name

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    companion object {
        const val EXTRA_PHONE_NUMBER = "extra_phone_number"
        private const val NOTIFICATION_CHANNEL_ID = "SECUREAPP_LOCATION_SERVICE_CHANNEL"
        private const val NOTIFICATION_ID = 102 // Use a unique ID for this service's notification
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val phoneNumber = intent?.getStringExtra(EXTRA_PHONE_NUMBER)
        if (phoneNumber.isNullOrEmpty()) {
            Log.w("LocationService", "Service started without a phone number. Stopping.")
            stopSelf()
            return START_NOT_STICKY
        }

        // Start the service in the foreground immediately
        startForeground(NOTIFICATION_ID, createNotification())

        // Request the current location instead of relying on the last known one
        requestCurrentLocation(phoneNumber)

        return START_NOT_STICKY
    }

    private fun requestCurrentLocation(phoneNumber: String) {
        // 1. CRITICAL: Check for location permission before making any location request.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("LocationService", "Location permission not granted. Cannot get location.")
            sendSms(phoneNumber, "Unable to fetch location: Permission denied.")
            stopSelf()
            return
        }

        // 2. IMPROVEMENT: Request a fresh, high-accuracy location update.
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            numUpdates = 1 // We only need one good location update
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                // Once we get a result, stop listening to save battery.
                fusedLocationClient.removeLocationUpdates(this)

                val location = locationResult.lastLocation
                if (location != null) {
                    val message = "Device location:\nLat=${location.latitude}, Lng=${location.longitude}\n" +
                            "Google Maps: https://maps.google.com/?q=${location.latitude},${location.longitude}"
                    sendSms(phoneNumber, message)
                } else {
                    // This is rare but possible if location could not be determined.
                    sendSms(phoneNumber, "Unable to fetch location: Failed to get a fix.")
                }
                stopSelf() // The work is done, stop the service.
            }
        }

        Log.d("LocationService", "Requesting current location update...")
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun createNotification(): Notification {
        // Create the NotificationChannel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Location Service",
                NotificationManager.IMPORTANCE_LOW // LOW is fine, as the user doesn't need to interact with it.
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(serviceChannel)
        }

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("SecureApp")
            .setContentText("Fetching device location...")
            // Use an icon from your app's drawable resources for better branding
            .setSmallIcon(R.drawable.ic_launcher_foreground) // CHANGE TO YOUR APP'S ICON
            .build()
    }

    private fun sendSms(phoneNumber: String, message: String) {
        try {
            // Use the default SmsManager. On modern devices, you don't need to specify a subscription.
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Log.i("LocationService", "SMS sent to $phoneNumber: $message")
        } catch (e: Exception) {
            Log.e("LocationService", "Failed to send SMS to $phoneNumber", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Ensure location updates are stopped if the service is destroyed unexpectedly.
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        // This is not a bound service, so return null.
        return null
    }
}
