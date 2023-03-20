package com.project.lagidimana.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.project.lagidimana.isMyServiceRunning
import com.project.lagidimana.service.Scheduler
import com.project.lagidimana.service.location.LocationService
import com.project.lagidimana.service.location.LocationWorker

class TimeChangedBroadcastReceiver: BroadcastReceiver(){
    override fun onReceive(context: Context, intent: Intent?) {
        val isLocationServiceRunning = context.isMyServiceRunning(LocationService::class.java)
        if(isLocationServiceRunning) {
            context.stopService(Intent(context, LocationService::class.java))
            LocationWorker.startWorker(context)
            Scheduler.scheduleTask(context)
        }
    }
}