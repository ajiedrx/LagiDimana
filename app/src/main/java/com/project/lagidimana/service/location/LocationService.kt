package com.project.lagidimana.service.location

import android.app.Service
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
import androidx.core.content.ContextCompat
import com.project.lagidimana.Const
import com.project.lagidimana.data.AppRepository
import com.project.lagidimana.data.model.LocationLogEntity
import org.koin.android.ext.android.inject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.concurrent.schedule


class LocationService: Service(), LocationListener {

    private var wakeLock: PowerManager.WakeLock? = null
    private var currentServiceNotification: ServiceNotification? = null

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
        currentService = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startWakeLock()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                currentServiceNotification = ServiceNotification(this, false)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(NOTIFICATION_ID, currentServiceNotification!!.notification!!, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
                } else {
                    startForeground(NOTIFICATION_ID, currentServiceNotification!!.notification)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting foreground process " + e.message)
            }
        }
        timer.apply {
            schedule(
                0, 300000
            ) {
                mHandler.post { getLocation() }
            }
        }
        return START_REDELIVER_INTENT
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
        if (isNetworkEnable) {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                1000,
                0f,
                this@LocationService
            )
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (location != null) {
                insertLocation(location, true)
            }
        } else {
            if (isGPSEnable) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0f, this)
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (location != null) {
                    insertLocation(location, false)
                }
            }
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
        currentService = null
        mHandler.removeMessages(0)
        timer.cancel()
        wakeLock?.release()
    }

    companion object{
        var currentService: LocationService? = null
        private val TAG = LocationService::class.java.simpleName
        private const val NOTIFICATION_ID = 9974
    }
}