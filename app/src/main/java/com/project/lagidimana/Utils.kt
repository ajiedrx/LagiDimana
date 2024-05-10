package com.project.lagidimana

import android.app.ActivityManager
import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ExecutionException


fun Context.isMyServiceRunning(serviceClass: Class<*>): Boolean {
    val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
    for (service in manager!!.getRunningServices(Int.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }
    return false
}

fun String.formatDate(currentFormat: String = Const.DEFAULT_DATA_DATE_TIME_PATTERN, targetFormat: String = Const.DEFAULT_READABLE_DATE_TIME_PATTERN): String {
    val defaultFormat = DateTimeFormatter.ofPattern(currentFormat)
    val readableFormat = DateTimeFormatter.ofPattern(targetFormat)
    val currentDateTime = LocalDateTime.parse(this, defaultFormat)
    return readableFormat.format(currentDateTime)
}

fun Context.isWorkScheduled(tag: String): Boolean {
    val instance = WorkManager.getInstance(this)
    val statuses = instance.getWorkInfosByTag(tag)
    return try {
        var running = false
        val workInfoList = statuses.get()
        for (workInfo in workInfoList) {
            val state = workInfo.state
            running = state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED
        }
        running
    } catch (e: ExecutionException) {
        e.printStackTrace()
        false
    } catch (e: InterruptedException) {
        e.printStackTrace()
        false
    }
}
