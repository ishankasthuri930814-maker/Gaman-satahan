package com.example.data.repository

import com.example.data.local.PlannedTripDao
import com.example.data.local.SettingsDao
import com.example.data.local.TripDao
import com.example.data.model.PlannedTrip
import com.example.data.model.TripLog
import com.example.data.model.VehicleSetting
import com.example.data.model.DriverSetting
import com.example.data.model.AssistantSetting
import kotlinx.coroutines.flow.Flow

class TripRepository(
    private val tripDao: TripDao,
    private val plannedTripDao: PlannedTripDao,
    private val settingsDao: SettingsDao
) {
    val allTripLogs: Flow<List<TripLog>> = tripDao.getAllTripLogs()
    val allPlannedTrips: Flow<List<PlannedTrip>> = plannedTripDao.getAllPlannedTrips()

    // Settings flows
    val allVehicles: Flow<List<VehicleSetting>> = settingsDao.getAllVehicles()
    val allDrivers: Flow<List<DriverSetting>> = settingsDao.getAllDrivers()
    val allAssistants: Flow<List<AssistantSetting>> = settingsDao.getAllAssistants()

    suspend fun insertTripLog(tripLog: TripLog): Long {
        return tripDao.insertTripLog(tripLog)
    }

    suspend fun deleteTripLogById(id: Int) {
        tripDao.deleteTripLogById(id)
    }

    suspend fun getPlannedTripsList(): List<PlannedTrip> {
        return plannedTripDao.getPlannedTripsList()
    }

    suspend fun getPlannedTripById(id: Int): PlannedTrip? {
        return plannedTripDao.getPlannedTripById(id)
    }

    suspend fun insertPlannedTrip(plannedTrip: PlannedTrip): Long {
        return plannedTripDao.insertPlannedTrip(plannedTrip)
    }

    suspend fun updatePlannedTrip(plannedTrip: PlannedTrip) {
        plannedTripDao.updatePlannedTrip(plannedTrip)
    }

    suspend fun deletePlannedTripById(id: Int) {
        plannedTripDao.deletePlannedTripById(id)
    }

    // Settings actions
    suspend fun insertVehicle(vehicle: VehicleSetting): Long {
        return settingsDao.insertVehicle(vehicle)
    }

    suspend fun deleteVehicleById(id: Int) {
        settingsDao.deleteVehicleById(id)
    }

    suspend fun deleteVehicleByPlate(plateNumber: String) {
        settingsDao.deleteVehicleByPlate(plateNumber)
    }

    suspend fun insertDriver(driver: DriverSetting): Long {
        return settingsDao.insertDriver(driver)
    }

    suspend fun deleteDriverById(id: Int) {
        settingsDao.deleteDriverById(id)
    }

    suspend fun deleteDriverByName(name: String) {
        settingsDao.deleteDriverByName(name)
    }

    suspend fun insertAssistant(assistant: AssistantSetting): Long {
        return settingsDao.insertAssistant(assistant)
    }

    suspend fun deleteAssistantById(id: Int) {
        settingsDao.deleteAssistantById(id)
    }

    suspend fun deleteAssistantByName(name: String) {
        settingsDao.deleteAssistantByName(name)
    }
}

