package com.project.lagidimana.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.project.lagidimana.presentation.model.LocationLog

@Entity
data class LocationLogEntity(
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("time")
    val time: String,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("isOnline")
    val isOnline: Boolean
) {
    fun toPresentation(): LocationLog{
        return LocationLog(
            time = time,
            latitude = latitude,
            longitude = longitude,
            isOnline = isOnline
        )
    }
}
