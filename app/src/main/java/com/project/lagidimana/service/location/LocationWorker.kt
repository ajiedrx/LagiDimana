package com.project.lagidimana.service.location

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.project.lagidimana.Const
import com.project.lagidimana.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocationWorker(private val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    companion object {
        private var NOTIFICATION_ID = 9973

        fun startWorker(context: Context) {
            val constraints = Constraints.Builder()
                .build()
            val locationWorker = OneTimeWorkRequestBuilder<LocationWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(constraints)
                .addTag(Const.LOCATION_WORKER)
                .build()
            val workManagerInstance = WorkManager.getInstance(context)
            workManagerInstance.cancelAllWorkByTag(Const.LOCATION_WORKER)
            workManagerInstance
                .enqueue(
                    listOf(locationWorker)
                )
        }
    }


    override suspend fun doWork(): Result {
        if (LocationService.currentService == null) {
            withContext(Dispatchers.IO) {
                val trackerServiceIntent = Intent(context, LocationService::class.java)
                ServiceNotification.notificationIcon = R.drawable.ic_location
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(trackerServiceIntent)
                } else {
                    context.startService(trackerServiceIntent)
                }
            }
        }
        return Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        ServiceNotification.notificationIcon = R.drawable.ic_location
        val notification = ServiceNotification(context, true)
        return ForegroundInfo(NOTIFICATION_ID, notification.notification!!)
    }
}