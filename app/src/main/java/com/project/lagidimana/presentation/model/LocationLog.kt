package com.project.lagidimana.presentation.model

import com.project.lagidimana.data.model.LocationLogEntity

data class LocationLog(
    val time: String,
    val latitude: Double,
    val longitude: Double,
    val isOnline: Boolean
) {
    fun toEntity(): LocationLogEntity{
        return LocationLogEntity(
            id = 0,
            time = time,
            latitude = latitude,
            longitude = longitude,
            isOnline = isOnline
        )
    }
}