package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trip_logs")
data class TripLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val destination: String,
    val reason: String,
    val vehicleName: String,
    val driverName: String,
    val assistantName: String,
    val distanceKm: Double,
    val dateTimeMillis: Long,
    val fuelOrderNumber: String = "",
    val fuelLiters: Double = 0.0
)

@Entity(tableName = "planned_trips")
data class PlannedTrip(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val destination: String,
    val notes: String,
    val scheduledTimeMillis: Long,
    val notifiedPrevDay: Boolean = false,
    val notifiedFewHours: Boolean = false,
    val notifiedOneHour: Boolean = false
)

@Entity(tableName = "vehicles")
data class VehicleSetting(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val plateNumber: String,
    val description: String = ""
)

@Entity(tableName = "drivers")
data class DriverSetting(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String = ""
)

@Entity(tableName = "assistants")
data class AssistantSetting(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String = ""
)

