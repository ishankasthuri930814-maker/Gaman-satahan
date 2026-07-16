package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm.TripAlarmScheduler
import com.example.data.local.AppDatabase
import com.example.data.model.PlannedTrip
import com.example.data.model.TripLog
import com.example.data.model.VehicleSetting
import com.example.data.model.DriverSetting
import com.example.data.model.AssistantSetting
import com.example.data.repository.TripRepository
import com.example.ui.translation.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class TripViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TripRepository
    private val prefs = application.getSharedPreferences("vahanalog_prefs", Context.MODE_PRIVATE)

    // Sheetson API configurations
    private val SHEETSON_API_KEY = "x467G3-QIam9I2sLvtETpX6Pj8hgd_VB2p-NmWMuOQS1TQoXLnBeqE5nPi4"
    private val SHEETSON_SPREADSHEET_ID = "1fxDVV0HtYwuGAj7iiPMMRzbzMNnE5NWrmEXMRyHg4NU"
    private val SHEETSON_SHEET_NAME = "Sheet1"

    // Sync status states
    val syncUrl = MutableStateFlow("")
    val isSyncing = MutableStateFlow(false)
    val lastSyncTime = MutableStateFlow(0L)

    // Dark Mode settings
    val isDarkMode = MutableStateFlow(prefs.getBoolean("is_dark_mode", false))

    fun setDarkMode(enabled: Boolean) {
        isDarkMode.value = enabled
        prefs.edit().putBoolean("is_dark_mode", enabled).apply()
    }
    
    // UI state streams from Database Room flows
    val tripLogs: StateFlow<List<TripLog>>
    val plannedTrips: StateFlow<List<PlannedTrip>>

    // Settings flows
    val vehicles: StateFlow<List<VehicleSetting>>
    val drivers: StateFlow<List<DriverSetting>>
    val assistants: StateFlow<List<AssistantSetting>>

    // App language selection state
    val currentLanguage = MutableStateFlow(AppLanguage.SINHALA) // Default to Sinhala for local user comfort

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TripRepository(database.tripDao(), database.plannedTripDao(), database.settingsDao())
        
        tripLogs = repository.allTripLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        plannedTrips = repository.allPlannedTrips.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        vehicles = repository.allVehicles.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        drivers = repository.allDrivers.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        assistants = repository.allAssistants.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Set hardcoded Sheetson URL as requested by the user, making it unchangeable and safe
        syncUrl.value = "https://api.sheetson.com/v2/sheets/$SHEETSON_SHEET_NAME"
        lastSyncTime.value = prefs.getLong("last_sync_time", 0L)

        // Auto sync on start and then periodically every 45 seconds automatically
        viewModelScope.launch {
            while (true) {
                if (syncUrl.value.isNotBlank()) {
                    syncWithGoogleSheets { _, _ -> }
                }
                kotlinx.coroutines.delay(45000) // 45 seconds background interval
            }
        }
    }

    fun setLanguage(language: AppLanguage) {
        currentLanguage.value = language
    }

    fun saveSyncUrl(url: String) {
        // Ignored to ensure the hardcoded SheetDB URL remains unchanged and completely secure
    }

    fun syncWithGoogleSheets(onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        val urlStr = syncUrl.value
        if (urlStr.isBlank()) {
            onResult(false, "Sync URL is not set!")
            return
        }

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            isSyncing.value = true
            try {
                // 1. Fetch current local states
                val localTrips = tripLogs.value
                val localVehicles = vehicles.value
                val localDrivers = drivers.value
                val localAssistants = assistants.value

                val pendingVehicles = prefs.getStringSet("pending_vehicles", emptySet()) ?: emptySet()
                val pendingDrivers = prefs.getStringSet("pending_drivers", emptySet()) ?: emptySet()
                val pendingAssistants = prefs.getStringSet("pending_assistants", emptySet()) ?: emptySet()
                val pendingTrips = prefs.getStringSet("pending_trips", emptySet()) ?: emptySet()

                // 2. Fetch all entries from Sheetson via GET
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                val getRequest = okhttp3.Request.Builder()
                    .url("https://api.sheetson.com/v2/sheets/$SHEETSON_SHEET_NAME")
                    .header("Authorization", "Bearer $SHEETSON_API_KEY")
                    .header("X-Spreadsheet-Id", SHEETSON_SPREADSHEET_ID)
                    .get()
                    .build()

                client.newCall(getRequest).execute().use { response ->
                    if (!response.isSuccessful) {
                        launch(kotlinx.coroutines.Dispatchers.Main) {
                            isSyncing.value = false
                            onResult(false, "HTTP Error: ${response.code}")
                        }
                        return@use
                    }

                    val responseBodyStr = response.body?.string() ?: "{\"results\":[]}"

                    // Parse response results array of server entries
                    val rootObj = org.json.JSONObject(responseBodyStr)
                    val serverArray = rootObj.optJSONArray("results") ?: org.json.JSONArray()
                    val serverTrips = mutableListOf<TripLog>()
                    val serverVehicles = mutableListOf<VehicleSetting>()
                    val serverDrivers = mutableListOf<DriverSetting>()
                    val serverAssistants = mutableListOf<AssistantSetting>()

                    for (i in 0 until serverArray.length()) {
                        val obj = serverArray.getJSONObject(i)
                        val destinationVal = obj.optString("destination", "")
                        if (destinationVal.startsWith("__CONFIG_VEHICLE__")) {
                            val plate = destinationVal.removePrefix("__CONFIG_VEHICLE__")
                            val desc = obj.optString("reason", "")
                            if (plate.isNotBlank()) {
                                serverVehicles.add(VehicleSetting(plateNumber = plate, description = desc))
                            }
                        } else if (destinationVal.startsWith("__CONFIG_DRIVER__")) {
                            val name = destinationVal.removePrefix("__CONFIG_DRIVER__")
                            val phone = obj.optString("reason", "")
                            if (name.isNotBlank()) {
                                serverDrivers.add(DriverSetting(name = name, phone = phone))
                            }
                        } else if (destinationVal.startsWith("__CONFIG_ASSISTANT__")) {
                            val name = destinationVal.removePrefix("__CONFIG_ASSISTANT__")
                            val phone = obj.optString("reason", "")
                            if (name.isNotBlank()) {
                                serverAssistants.add(AssistantSetting(name = name, phone = phone))
                            }
                        } else {
                            val trip = TripLog(
                                destination = destinationVal,
                                reason = obj.optString("reason", ""),
                                vehicleName = obj.optString("vehicleName", ""),
                                driverName = obj.optString("driverName", ""),
                                assistantName = obj.optString("assistantName", ""),
                                distanceKm = obj.optDouble("distanceKm", 0.0),
                                dateTimeMillis = obj.optLong("dateTimeMillis", 0L),
                                fuelOrderNumber = obj.optString("fuelOrderNumber", ""),
                                fuelLiters = obj.optDouble("fuelLiters", 0.0)
                            )
                            serverTrips.add(trip)
                        }
                    }

                    // 3. Add server trips that are missing locally to local DB
                    var newTripsAdded = 0
                    for (serverTrip in serverTrips) {
                        val exists = localTrips.any { local ->
                            local.destination.trim().equals(serverTrip.destination.trim(), ignoreCase = true) &&
                            local.vehicleName.trim().equals(serverTrip.vehicleName.trim(), ignoreCase = true) &&
                            local.driverName.trim().equals(serverTrip.driverName.trim(), ignoreCase = true) &&
                            local.dateTimeMillis == serverTrip.dateTimeMillis
                        }

                        if (!exists) {
                            repository.insertTripLog(serverTrip)
                            newTripsAdded++
                        }
                    }

                    // 4. Find local trips that are missing on server and upload them via POST if pending
                    val tripsToUpload = mutableListOf<TripLog>()
                    for (localTrip in localTrips) {
                        val existsOnServer = serverTrips.any { serverTrip ->
                            localTrip.destination.trim().equals(serverTrip.destination.trim(), ignoreCase = true) &&
                            localTrip.vehicleName.trim().equals(serverTrip.vehicleName.trim(), ignoreCase = true) &&
                            localTrip.driverName.trim().equals(serverTrip.driverName.trim(), ignoreCase = true) &&
                            localTrip.dateTimeMillis == serverTrip.dateTimeMillis
                        }
                        if (!existsOnServer) {
                            val isPending = localTrip.dateTimeMillis.toString() in pendingTrips
                            if (isPending) {
                                tripsToUpload.add(localTrip)
                            } else {
                                // Already synced before but deleted on server, so delete locally as well
                                repository.deleteTripLogById(localTrip.id)
                            }
                        }
                    }

                    // 5. Gather configurations to upload
                    val configsToUpload = mutableListOf<org.json.JSONObject>()

                    val updatedPendingVehicles = pendingVehicles.toMutableSet()
                    for (lv in localVehicles) {
                        val isPending = lv.plateNumber in pendingVehicles
                        val existsOnServer = serverVehicles.any { it.plateNumber == lv.plateNumber }
                        if (isPending || !existsOnServer) {
                            val obj = org.json.JSONObject()
                            obj.put("destination", "__CONFIG_VEHICLE__${lv.plateNumber}")
                            obj.put("reason", lv.description)
                            obj.put("vehicleName", "")
                            obj.put("driverName", "")
                            obj.put("assistantName", "")
                            obj.put("distanceKm", 0.0)
                            obj.put("dateTimeMillis", System.currentTimeMillis())
                            configsToUpload.add(obj)
                        }
                    }

                    val updatedPendingDrivers = pendingDrivers.toMutableSet()
                    for (ld in localDrivers) {
                        val isPending = ld.name in pendingDrivers
                        val existsOnServer = serverDrivers.any { it.name == ld.name }
                        if (isPending || !existsOnServer) {
                            val obj = org.json.JSONObject()
                            obj.put("destination", "__CONFIG_DRIVER__${ld.name}")
                            obj.put("reason", ld.phone)
                            obj.put("vehicleName", "")
                            obj.put("driverName", "")
                            obj.put("assistantName", "")
                            obj.put("distanceKm", 0.0)
                            obj.put("dateTimeMillis", System.currentTimeMillis())
                            configsToUpload.add(obj)
                        }
                    }

                    val updatedPendingAssistants = pendingAssistants.toMutableSet()
                    for (la in localAssistants) {
                        val isPending = la.name in pendingAssistants
                        val existsOnServer = serverAssistants.any { it.name == la.name }
                        if (isPending || !existsOnServer) {
                            val obj = org.json.JSONObject()
                            obj.put("destination", "__CONFIG_ASSISTANT__${la.name}")
                            obj.put("reason", la.phone)
                            obj.put("vehicleName", "")
                            obj.put("driverName", "")
                            obj.put("assistantName", "")
                            obj.put("distanceKm", 0.0)
                            obj.put("dateTimeMillis", System.currentTimeMillis())
                            configsToUpload.add(obj)
                        }
                    }

                    val updatedPendingTrips = pendingTrips.toMutableSet()
                    var hasUploadErrors = false

                    // Send individual POST requests to Sheetson for each row
                    for (trip in tripsToUpload) {
                        val obj = org.json.JSONObject()
                        obj.put("destination", trip.destination)
                        obj.put("reason", trip.reason)
                        obj.put("vehicleName", trip.vehicleName)
                        obj.put("driverName", trip.driverName)
                        obj.put("assistantName", trip.assistantName)
                        obj.put("distanceKm", trip.distanceKm)
                        obj.put("dateTimeMillis", trip.dateTimeMillis)
                        obj.put("fuelOrderNumber", trip.fuelOrderNumber)
                        obj.put("fuelLiters", trip.fuelLiters)

                        val requestBody = obj.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                        val postRequest = okhttp3.Request.Builder()
                            .url("https://api.sheetson.com/v2/sheets/$SHEETSON_SHEET_NAME")
                            .header("Authorization", "Bearer $SHEETSON_API_KEY")
                            .header("X-Spreadsheet-Id", SHEETSON_SPREADSHEET_ID)
                            .post(requestBody)
                            .build()

                        try {
                            client.newCall(postRequest).execute().use { postResponse ->
                                if (postResponse.isSuccessful) {
                                    updatedPendingTrips.remove(trip.dateTimeMillis.toString())
                                } else {
                                    hasUploadErrors = true
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            hasUploadErrors = true
                        }
                    }

                    for (config in configsToUpload) {
                        val requestBody = config.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                        val postRequest = okhttp3.Request.Builder()
                            .url("https://api.sheetson.com/v2/sheets/$SHEETSON_SHEET_NAME")
                            .header("Authorization", "Bearer $SHEETSON_API_KEY")
                            .header("X-Spreadsheet-Id", SHEETSON_SPREADSHEET_ID)
                            .post(requestBody)
                            .build()

                        try {
                            client.newCall(postRequest).execute().use { postResponse ->
                                if (postResponse.isSuccessful) {
                                    val dest = config.optString("destination")
                                    if (dest.startsWith("__CONFIG_VEHICLE__")) {
                                        val plate = dest.removePrefix("__CONFIG_VEHICLE__")
                                        updatedPendingVehicles.remove(plate)
                                    } else if (dest.startsWith("__CONFIG_DRIVER__")) {
                                        val name = dest.removePrefix("__CONFIG_DRIVER__")
                                        updatedPendingDrivers.remove(name)
                                    } else if (dest.startsWith("__CONFIG_ASSISTANT__")) {
                                        val name = dest.removePrefix("__CONFIG_ASSISTANT__")
                                        updatedPendingAssistants.remove(name)
                                    }
                                } else {
                                    hasUploadErrors = true
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            hasUploadErrors = true
                        }
                    }

                    // Save updated pending lists
                    prefs.edit()
                        .putStringSet("pending_trips", updatedPendingTrips)
                        .putStringSet("pending_vehicles", updatedPendingVehicles)
                        .putStringSet("pending_drivers", updatedPendingDrivers)
                        .putStringSet("pending_assistants", updatedPendingAssistants)
                        .apply()

                    // 6. Align local databases with server configuration
                    for (sv in serverVehicles) {
                        val existsLocally = localVehicles.any { it.plateNumber == sv.plateNumber }
                        if (!existsLocally) {
                            repository.insertVehicle(sv)
                        }
                    }
                    for (lv in localVehicles) {
                        val existsOnServer = serverVehicles.any { it.plateNumber == lv.plateNumber }
                        val isPending = lv.plateNumber in pendingVehicles
                        if (!existsOnServer && !isPending) {
                            repository.deleteVehicleByPlate(lv.plateNumber)
                        }
                    }

                    for (sd in serverDrivers) {
                        val existsLocally = localDrivers.any { it.name == sd.name }
                        if (!existsLocally) {
                            repository.insertDriver(sd)
                        }
                    }
                    for (ld in localDrivers) {
                        val existsOnServer = serverDrivers.any { it.name == ld.name }
                        val isPending = ld.name in pendingDrivers
                        if (!existsOnServer && !isPending) {
                            repository.deleteDriverByName(ld.name)
                        }
                    }

                    for (sa in serverAssistants) {
                        val existsLocally = localAssistants.any { it.name == sa.name }
                        if (!existsLocally) {
                            repository.insertAssistant(sa)
                        }
                    }
                    for (la in localAssistants) {
                        val existsOnServer = serverAssistants.any { it.name == la.name }
                        val isPending = la.name in pendingAssistants
                        if (!existsOnServer && !isPending) {
                            repository.deleteAssistantByName(la.name)
                        }
                    }

                    // Save sync time
                    val now = System.currentTimeMillis()
                    prefs.edit().putLong("last_sync_time", now).apply()
                    lastSyncTime.value = now

                    launch(kotlinx.coroutines.Dispatchers.Main) {
                        isSyncing.value = false
                        onResult(true, "Synced successfully! Added $newTripsAdded new trip(s) from cloud.")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                launch(kotlinx.coroutines.Dispatchers.Main) {
                    isSyncing.value = false
                    onResult(false, "Sync failed: ${e.message}")
                }
            }
        }
    }

    fun addTripLog(
        destination: String,
        reason: String,
        vehicleName: String,
        driverName: String,
        assistantName: String,
        distanceKm: Double,
        dateTimeMillis: Long,
        fuelOrderNumber: String = "",
        fuelLiters: Double = 0.0
    ) {
        viewModelScope.launch {
            val log = TripLog(
                destination = destination,
                reason = reason,
                vehicleName = vehicleName,
                driverName = driverName,
                assistantName = assistantName,
                distanceKm = distanceKm,
                dateTimeMillis = dateTimeMillis,
                fuelOrderNumber = fuelOrderNumber,
                fuelLiters = fuelLiters
            )
            repository.insertTripLog(log)
            
            // Mark trip as pending upload
            val pending = prefs.getStringSet("pending_trips", emptySet())?.toMutableSet() ?: mutableSetOf()
            pending.add(dateTimeMillis.toString())
            prefs.edit().putStringSet("pending_trips", pending).apply()
            
            // Trigger auto sync in background if URL is set
            if (syncUrl.value.isNotBlank()) {
                syncWithGoogleSheets()
            }
        }
    }

    fun deleteTripLog(id: Int) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val tripLog = tripLogs.value.find { it.id == id }
            repository.deleteTripLogById(id)
            
            if (tripLog != null) {
                // Remove from pending trips
                val pending = prefs.getStringSet("pending_trips", emptySet())?.toMutableSet() ?: mutableSetOf()
                if (pending.remove(tripLog.dateTimeMillis.toString())) {
                    prefs.edit().putStringSet("pending_trips", pending).apply()
                }
                
                // Call SheetDB DELETE API to remove it from server
                val urlStr = syncUrl.value
                if (urlStr.isNotBlank()) {
                    try {
                        val client = okhttp3.OkHttpClient.Builder()
                            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                            .build()
                        
                        val deleteUrl = "$urlStr/dateTimeMillis/${tripLog.dateTimeMillis}"
                        val request = okhttp3.Request.Builder()
                            .url(deleteUrl)
                            .delete()
                            .build()
                        
                        client.newCall(request).execute().use { response ->
                            if (response.isSuccessful) {
                                // Deleted from server successfully
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun addPlannedTrip(
        title: String,
        destination: String,
        notes: String,
        scheduledTimeMillis: Long
    ) {
        viewModelScope.launch {
            val planned = PlannedTrip(
                title = title,
                destination = destination,
                notes = notes,
                scheduledTimeMillis = scheduledTimeMillis
            )
            val insertedId = repository.insertPlannedTrip(planned)
            
            // Re-fetch the saved entity to get the correct auto-generated ID, or use it directly
            val fullySavedTrip = planned.copy(id = insertedId.toInt())
            
            // Schedule Alarms
            TripAlarmScheduler.scheduleAlarmsForTrip(getApplication(), fullySavedTrip)
        }
    }

    fun deletePlannedTrip(id: Int) {
        viewModelScope.launch {
            // Cancel any scheduled alarms
            TripAlarmScheduler.cancelAlarmsForTrip(getApplication(), id)
            // Delete from Database
            repository.deletePlannedTripById(id)
        }
    }

    private fun deleteConfigFromServer(destinationKey: String) {
        val urlStr = syncUrl.value
        if (urlStr.isBlank()) return
        
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                
                val encodedKey = java.net.URLEncoder.encode(destinationKey, "UTF-8")
                val deleteUrl = "$urlStr/destination/$encodedKey"
                
                val request = okhttp3.Request.Builder()
                    .url(deleteUrl)
                    .delete()
                    .build()
                
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        // Deleted successfully
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Vehicle Configuration Methods
    fun addVehicle(plateNumber: String, description: String) {
        viewModelScope.launch {
            repository.insertVehicle(VehicleSetting(plateNumber = plateNumber, description = description))
            val pending = prefs.getStringSet("pending_vehicles", emptySet())?.toMutableSet() ?: mutableSetOf()
            pending.add(plateNumber)
            prefs.edit().putStringSet("pending_vehicles", pending).apply()
            syncWithGoogleSheets()
        }
    }

    fun deleteVehicle(id: Int, plateNumber: String) {
        viewModelScope.launch {
            repository.deleteVehicleById(id)
            val pending = prefs.getStringSet("pending_vehicles", emptySet())?.toMutableSet() ?: mutableSetOf()
            if (pending.remove(plateNumber)) {
                prefs.edit().putStringSet("pending_vehicles", pending).apply()
            }
            deleteConfigFromServer("__CONFIG_VEHICLE__$plateNumber")
        }
    }

    // Driver Configuration Methods
    fun addDriver(name: String, phone: String) {
        viewModelScope.launch {
            repository.insertDriver(DriverSetting(name = name, phone = phone))
            val pending = prefs.getStringSet("pending_drivers", emptySet())?.toMutableSet() ?: mutableSetOf()
            pending.add(name)
            prefs.edit().putStringSet("pending_drivers", pending).apply()
            syncWithGoogleSheets()
        }
    }

    fun deleteDriver(id: Int, name: String) {
        viewModelScope.launch {
            repository.deleteDriverById(id)
            val pending = prefs.getStringSet("pending_drivers", emptySet())?.toMutableSet() ?: mutableSetOf()
            if (pending.remove(name)) {
                prefs.edit().putStringSet("pending_drivers", pending).apply()
            }
            deleteConfigFromServer("__CONFIG_DRIVER__$name")
        }
    }

    // Assistant Configuration Methods
    fun addAssistant(name: String, phone: String) {
        viewModelScope.launch {
            repository.insertAssistant(AssistantSetting(name = name, phone = phone))
            val pending = prefs.getStringSet("pending_assistants", emptySet())?.toMutableSet() ?: mutableSetOf()
            pending.add(name)
            prefs.edit().putStringSet("pending_assistants", pending).apply()
            syncWithGoogleSheets()
        }
    }

    fun deleteAssistant(id: Int, name: String) {
        viewModelScope.launch {
            repository.deleteAssistantById(id)
            val pending = prefs.getStringSet("pending_assistants", emptySet())?.toMutableSet() ?: mutableSetOf()
            if (pending.remove(name)) {
                prefs.edit().putStringSet("pending_assistants", pending).apply()
            }
            deleteConfigFromServer("__CONFIG_ASSISTANT__$name")
        }
    }
}

