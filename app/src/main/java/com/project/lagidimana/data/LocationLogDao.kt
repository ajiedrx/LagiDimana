package com.project.lagidimana.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.project.lagidimana.data.model.LocationLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationLogDao {
    @Query("SELECT * FROM LocationLogEntity ORDER BY time DESC")
    fun getAll(): Flow<List<LocationLogEntity>>

    @Query("DELETE from LocationLogEntity")
    fun deleteAll()

    @Insert
    fun insert(data: LocationLogEntity)
}