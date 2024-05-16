package com.project.lagidimana

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat

object Const {
    const val CLEAR_DATA_TASK = 1189
    const val NOTIFICATION_CHANNEL_ID = "lagi_dimana_NotificationChannelID_01"
    const val NOTIFICATION_CHANNEL_NAME = "lagi_dimana_NotificationChannelName"
    const val NOTIFICATION_CHANNEL_DESC = "lagi_dimana_NotificationChannelDesc"
    const val CLEAR_DATA_NOTIFICATION_ID = 1314

    const val DEFAULT_READABLE_DATE_TIME_PATTERN = "dd MMMM yyyy HH:mm"
    const val DEFAULT_DATA_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm"

    fun Activity.isBackgroundLocationPermissionEnabled() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
}