package com.project.lagidimana

import android.app.ActivityManager
import android.content.Context
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


fun Context.isMyServiceRunning(serviceClass: Class<*>): Boolean {
    val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
    for (service in manager!!.getRunningServices(Int.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }
    return false
}

fun String.changeDateFormat(currentFormat: String = Const.DEFAULT_DATA_DATE_TIME_PATTERN, targetFormat: String = Const.DEFAULT_READABLE_DATE_TIME_PATTERN): String{
    val defaultFormat = DateTimeFormatter.ofPattern(currentFormat)
    val readableFormat = DateTimeFormatter.ofPattern(targetFormat)
    val currentDateTime = LocalDateTime.parse(this, defaultFormat)
    return readableFormat.format(currentDateTime)
}
