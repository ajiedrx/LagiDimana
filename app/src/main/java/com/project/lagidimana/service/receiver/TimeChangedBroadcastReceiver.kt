package com.project.lagidimana.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.project.lagidimana.isMyServiceRunning
import com.project.lagidimana.service.Scheduler
import com.project.lagidimana.service.location.LocationService

class TimeChangedBroadcastReceiver: BroadcastReceiver(){
    override fun onReceive(context: Context, intent: Intent?) {
        val isLocationServiceRunning = context.isMyServiceRunning(LocationService::class.java)
        if(isLocationServiceRunning) {
            LocationService.stop(context)
            LocationService.start(context)
            Scheduler.scheduleTask(context)
        }
    }
}