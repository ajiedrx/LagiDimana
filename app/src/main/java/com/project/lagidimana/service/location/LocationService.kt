package com.project.lagidimana.service.location

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.project.lagidimana.Const
import com.project.lagidimana.R
import com.project.lagidimana.data.AppRepository
import com.project.lagidimana.data.model.LocationLogEntity
import org.koin.android.ext.android.inject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.concurrent.schedule


class LocationService: Service(), LocationListener {

    private val foregroundNotificationId: Int = (System.currentTimeMillis() % 10000).toInt()
    private val foregroundNotification by lazy {
        NotificationCompat.Builder(this, foregroundNotificationChannelId)
            .setSmallIcon(R.drawable.ic_location)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSound(null)
            .setOngoing(true)
            .build()
    }
    private val foregroundNotificationChannelName by lazy {
        Const.NOTIFICATION_CHANNEL_NAME
    }
    private val foregroundNotificationChannelDescription by lazy {
        Const.NOTIFICATION_CHANNEL_DESC
    }
    private val foregroundNotificationChannelId by lazy {
        "ForegroundServiceSample.NotificationChannel".also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
                    if (getNotificationChannel(it) == null) {
                        createNotificationChannel(
                            NotificationChannel(
                            it,
                            foregroundNotificationChannelName,
                            NotificationManager.IMPORTANCE_MIN
                        ).also {
                            it.description = foregroundNotificationChannelDescription
                            it.lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
                            it.vibrationPattern = null
                            it.setSound(null, null)
                            it.setShowBadge(false)
                        })
                    }
                }
            }
        }
    }

    private var wakeLock: PowerManager.WakeLock? = null

    private val appRepository: AppRepository by inject()

    private val mHandler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    private val timer: Timer by lazy {
        Timer()
    }

    private val locationManager: LocationManager by lazy {
        applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startWakeLock()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(foregroundNotificationId, foregroundNotification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
                } else {
                    startForeground(foregroundNotificationId, foregroundNotification)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting foreground process " + e.message)
            }
        }
        timer.apply {
            schedule(
                0, 3000
            ) {
                mHandler.post { getLocation() }
            }
        }
        return START_STICKY
    }

    private fun startWakeLock(){
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG)
        wakeLock?.acquire()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onLocationChanged(p0: Location) {

    }
    
    private fun getLocation(){
        val isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        val location: Location?

        if ( Build.VERSION.SDK_INT >= 23 &&
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        if(Build.VERSION.SDK_INT >= 29 && ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return
        }
        locationManager.requestLocationUpdates(
            if(isNetworkEnable) LocationManager.NETWORK_PROVIDER else LocationManager.GPS_PROVIDER,
            1000,
            0f,
            this@LocationService
        )
        location = locationManager.getLastKnownLocation(if(isNetworkEnable) LocationManager.NETWORK_PROVIDER else LocationManager.GPS_PROVIDER)
        if (location != null) {
            insertLocation(location, true)
            locationManager.removeUpdates(this)
        }
    }

    private fun insertLocation(location: Location, isOnline: Boolean){
        val formatter = DateTimeFormatter.ofPattern(Const.DEFAULT_DATA_DATE_TIME_PATTERN)
        appRepository.insertData(
            LocationLogEntity(
                time = LocalDateTime.now().format(formatter),
                latitude = location.latitude,
                longitude = location.longitude,
                isOnline = isOnline
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeMessages(0)
        timer.cancel()
        wakeLock?.release()
    }

    companion object{
        private val TAG = LocationService::class.java.simpleName
        @JvmStatic
        fun start(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, LocationService::class.java))
        }

        @JvmStatic
        fun stop(context: Context) {
            context.stopService(Intent(context, LocationService::class.java))
        }
    }
}