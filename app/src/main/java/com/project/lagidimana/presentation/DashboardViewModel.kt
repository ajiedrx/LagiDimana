package com.project.lagidimana.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.project.lagidimana.data.AppRepository
import com.project.lagidimana.isMyServiceRunning
import com.project.lagidimana.service.Scheduler.scheduleTask
import com.project.lagidimana.service.location.LocationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class DashboardViewModel(
    private val application: Application,
    private val repository: AppRepository
) : AndroidViewModel(application) {

    val isServiceRunning: StateFlow<Boolean> by lazy { _isServiceRunning.asStateFlow() }
    private val _isServiceRunning: MutableStateFlow<Boolean> = MutableStateFlow(application.isMyServiceRunning(LocationService::class.java))
    

    fun startService() {
        _isServiceRunning.value = true
        LocationService.start(application)
        scheduleTask(application)
    }

    fun getLocationLog() = repository.getAllLocationLog().map { it.map { it.toPresentation() } }
}