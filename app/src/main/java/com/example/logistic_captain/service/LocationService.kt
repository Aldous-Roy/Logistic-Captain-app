package com.example.logistic_captain.service

import android.app.*
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.logistic_captain.R
import com.example.logistic_captain.data.AppDatabase
import com.example.logistic_captain.data.LocationEntity
import com.example.logistic_captain.data.RetrofitClient
import com.example.logistic_captain.model.LocationUpdateRequest
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class LocationService : Service() {

    companion object {
        const val ACTION_PAUSE = "com.example.logistic_captain.PAUSE_TRACKING"
        const val ACTION_RESUME = "com.example.logistic_captain.RESUME_TRACKING"
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isTracking = false
    private var isPaused = false

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (!isPaused) {
                    locationResult.lastLocation?.let { location ->
                        handleLocationUpdate(location)
                    }
                }
            }
        }
    }

    private fun handleLocationUpdate(location: Location) {
        serviceScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val entity = LocationEntity(
                latitude = location.latitude,
                longitude = location.longitude
            )
            
            try {
                val response = RetrofitClient.apiService.updateLocation(
                    LocationUpdateRequest(location.latitude, location.longitude)
                )
                if (response.isSuccessful) {
                    db.locationDao().insert(entity.copy(isSynced = true))
                    Log.d("LocationService", "Location synced: ${location.latitude}, ${location.longitude}")
                } else {
                    db.locationDao().insert(entity)
                    Log.d("LocationService", "Location buffered (Server error)")
                }
            } catch (e: Exception) {
                db.locationDao().insert(entity)
                Log.d("LocationService", "Location buffered (Offline)")
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE -> {
                isPaused = true
                fusedLocationClient.removeLocationUpdates(locationCallback)
                Log.d("LocationService", "Location tracking PAUSED (driver on break)")
                return START_STICKY
            }
            ACTION_RESUME -> {
                isPaused = false
                requestLocationUpdates()
                Log.d("LocationService", "Location tracking RESUMED")
                return START_STICKY
            }
        }

        if (!isTracking) {
            startForegroundService()
            requestLocationUpdates()
            isTracking = true
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "location_channel"
        val notificationManager = getSystemService(NotificationManager::class.java)
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Logistic Captain")
            .setContentText("Tracking location for active shift...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)
    }

    private fun requestLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, TimeUnit.SECONDS.toMillis(30))
            .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(30))
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (unlikely: SecurityException) {
            Log.e("LocationService", "Lost location permission. $unlikely")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        serviceScope.cancel()
    }
}
