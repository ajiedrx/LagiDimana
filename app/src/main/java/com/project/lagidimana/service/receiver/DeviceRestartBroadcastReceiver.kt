package com.project.lagidimana.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.project.lagidimana.service.Scheduler.scheduleTask
import com.project.lagidimana.service.location.LocationWorker.Companion.startWorker

class DeviceRestartBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        startWorker(context)
        scheduleTask(context)
    }
}

