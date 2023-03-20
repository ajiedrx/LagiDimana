package com.project.lagidimana.data

import com.project.lagidimana.data.model.LocationLogEntity
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    fun insertData(data: LocationLogEntity)
    fun getAllLocationLog(): Flow<List<LocationLogEntity>>
    fun deleteAll()
}