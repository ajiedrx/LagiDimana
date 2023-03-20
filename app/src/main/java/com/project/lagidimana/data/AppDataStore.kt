package com.project.lagidimana.data

import com.project.lagidimana.data.model.LocationLogEntity
import kotlinx.coroutines.flow.Flow

class AppDataStore(private val dao: LocationLogDao): AppRepository {
    override fun insertData(data: LocationLogEntity) {
        dao.insert(data)
    }

    override fun getAllLocationLog(): Flow<List<LocationLogEntity>> {
        return dao.getAll()
    }

    override fun deleteAll() {
        dao.deleteAll()
    }
}