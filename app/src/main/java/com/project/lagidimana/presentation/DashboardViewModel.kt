package com.project.lagidimana.presentation

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.project.lagidimana.data.AppRepository
import com.project.lagidimana.isMyServiceRunning
import com.project.lagidimana.service.Scheduler.scheduleTask
import com.project.lagidimana.service.location.LocationService
import kotlinx.coroutines.flow.map

class DashboardViewModel(application: Application, private val repository: AppRepository): AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val context: Context = application

    val isServiceRunning: LiveData<Boolean> by lazy { _isServiceRunning }
    private val _isServiceRunning: MutableLiveData<Boolean> = MutableLiveData()

    init {
        _isServiceRunning.value = context.isMyServiceRunning(LocationService::class.java)
    }

    fun startService() {
        _isServiceRunning.value = true
        LocationService.start(context)
        scheduleTask(context)
    }

    fun getLocationLog() = repository.getAllLocationLog().map { it.map { it.toPresentation() } }.asLiveData()
}