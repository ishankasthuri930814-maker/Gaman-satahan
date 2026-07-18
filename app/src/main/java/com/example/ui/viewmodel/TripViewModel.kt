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
import com.example.data.model.OdometerCalculation
import com.example.data.repository.TripRepository
import com.example.ui.translation.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
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

    // Sync status states
    val syncUrl = MutableStateFlow("")
    val isSyncing = MutableStateFlow(false)
    val lastSyncTime = MutableStateFlow(0L)
    val syncErrorMessage = MutableStateFlow<String?>(null)
    val syncCooldownRemaining = MutableStateFlow(0L) // Remaining seconds of rate-limit cooldown
    private var cooldownUntil = 0L

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
    val odometerCalculations: StateFlow<List<OdometerCalculation>>

    // App language selection state
    val currentLanguage = MutableStateFlow(AppLanguage.SINHALA) // Default to Sinhala for local user comfort

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TripRepository(
            database.tripDao(),
            database.plannedTripDao(),
            database.settingsDao(),
            database.odometerCalculationDao()
        )
        
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

        odometerCalculations = repository.allOdometerCalculations.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Load SheetDB URL from SharedPreferences, falling back to default
        val defaultUrl = "https://sheetdb.io/api/v1/qnwn1fg1i4k27"
        val savedUrl = prefs.getString("sync_url", defaultUrl) ?: defaultUrl
        if (savedUrl == "https://sheetdb.io/api/v1/jmizl4njqajdl" || savedUrl.isBlank() || savedUrl == defaultUrl) {
            prefs.edit().putString("sync_url", defaultUrl).apply()
            syncUrl.value = defaultUrl
        } else {
            syncUrl.value = savedUrl
        }
        lastSyncTime.value = prefs.getLong("last_sync_time", 0L)

        // Clear all vehicles, drivers, assistants on first launch after this update so the user can enter everything anew
        val hasCleared = prefs.getBoolean("has_cleared_default_settings_v4", false)
        if (!hasCleared) {
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    repository.deleteAllVehicles()
                    repository.deleteAllDrivers()
                    repository.deleteAllAssistants()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            prefs.edit().putBoolean("has_cleared_default_settings_v4", true).apply()
        }

        // Smart Cooldown Countdown Timer
        viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                if (now < cooldownUntil) {
                    syncCooldownRemaining.value = ((cooldownUntil - now) / 1000).coerceAtLeast(0)
                } else {
                    if (syncCooldownRemaining.value > 0) {
                        syncCooldownRemaining.value = 0L
                        syncErrorMessage.value = null // Clear error when cooldown completes
                    }
                }
                kotlinx.coroutines.delay(1000)
            }
        }

        // Smart Auto Sync on Start and then every 90 seconds (near real-time but quota-safe)
        viewModelScope.launch {
            // Wait a brief moment on startup to let local database initialize
            kotlinx.coroutines.delay(2000)
            while (true) {
                if (syncUrl.value.isNotBlank() && System.currentTimeMillis() >= cooldownUntil) {
                    syncWithGoogleSheetsBackground()
                }
                kotlinx.coroutines.delay(90000) // 90 seconds background interval (uses 6x less quota than 15s)
            }
        }
    }

    fun setLanguage(language: AppLanguage) {
        currentLanguage.value = language
    }

    fun saveSyncUrl(url: String) {
        val cleanUrl = url.trim()
        syncUrl.value = cleanUrl
        prefs.edit().putString("sync_url", cleanUrl).apply()
    }

    fun syncWithGoogleSheets(onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        syncWithGoogleSheetsInternal(isManual = true, onResult = onResult)
    }

    fun syncWithGoogleSheetsBackground() {
        syncWithGoogleSheetsInternal(isManual = false, onResult = { _, _ -> })
    }

    private fun syncWithGoogleSheetsInternal(isManual: Boolean, onResult: (Boolean, String) -> Unit) {
        val urlStr = syncUrl.value
        if (urlStr.isBlank()) {
            onResult(false, "Sync URL is not set!")
            return
        }

        val nowTime = System.currentTimeMillis()
        if (nowTime < cooldownUntil) {
            val remainingSecs = ((cooldownUntil - nowTime) / 1000).coerceAtLeast(1)
            val msg = if (currentLanguage.value == AppLanguage.SINHALA) {
                "සමමුහුර්ත කිරීම තාවකාලිකව නවතා ඇත (Cooldown). තව තත්පර $remainingSecs කින් නැවත උත්සාහ කරන්න."
            } else {
                "Sync is on rate-limit cooldown. Try again in $remainingSecs seconds."
            }
            if (isManual) {
                onResult(false, msg)
            }
            return
        }

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            isSyncing.value = true
            try {
                // 1. Fetch current local states from database flows using .first() to prevent race conditions or stale StateFlow values
                val localTrips = repository.allTripLogs.first()
                val localVehicles = repository.allVehicles.first()
                val localDrivers = repository.allDrivers.first()
                val localAssistants = repository.allAssistants.first()

                val pendingVehicles = prefs.getStringSet("pending_vehicles", emptySet()) ?: emptySet()
                val pendingDrivers = prefs.getStringSet("pending_drivers", emptySet()) ?: emptySet()
                val pendingAssistants = prefs.getStringSet("pending_assistants", emptySet()) ?: emptySet()
                val pendingTrips = prefs.getStringSet("pending_trips", emptySet()) ?: emptySet()

                val pendingDeletionsTrips = prefs.getStringSet("pending_deletions_trips", emptySet()) ?: emptySet()
                val pendingDeletionsVehicles = prefs.getStringSet("pending_deletions_vehicles", emptySet()) ?: emptySet()
                val pendingDeletionsDrivers = prefs.getStringSet("pending_deletions_drivers", emptySet()) ?: emptySet()
                val pendingDeletionsAssistants = prefs.getStringSet("pending_deletions_assistants", emptySet()) ?: emptySet()

                // 2. Setup OkHttpClient with timeouts
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                // 3. Robust Offline-First Sync: Retry executing any pending deletes on the server
                val updatedPendingDeletionsTrips = pendingDeletionsTrips.toMutableSet()
                for (dt in pendingDeletionsTrips) {
                    try {
                        val deleteUrl = "$urlStr/dateTimeMillis/$dt"
                        val request = okhttp3.Request.Builder().url(deleteUrl).delete().build()
                        client.newCall(request).execute().use { response ->
                            if (response.isSuccessful) {
                                updatedPendingDeletionsTrips.remove(dt)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                val updatedPendingDeletionsVehicles = pendingDeletionsVehicles.toMutableSet()
                for (dv in pendingDeletionsVehicles) {
                    try {
                        val encodedKey = java.net.URLEncoder.encode("__CONFIG_VEHICLE__$dv", "UTF-8")
                        val deleteUrl = "$urlStr/destination/$encodedKey"
                        val request = okhttp3.Request.Builder().url(deleteUrl).delete().build()
                        client.newCall(request).execute().use { response ->
                            if (response.isSuccessful) {
                                updatedPendingDeletionsVehicles.remove(dv)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                val updatedPendingDeletionsDrivers = pendingDeletionsDrivers.toMutableSet()
                for (dd in pendingDeletionsDrivers) {
                    try {
                        val encodedKey = java.net.URLEncoder.encode("__CONFIG_DRIVER__$dd", "UTF-8")
                        val deleteUrl = "$urlStr/destination/$encodedKey"
                        val request = okhttp3.Request.Builder().url(deleteUrl).delete().build()
                        client.newCall(request).execute().use { response ->
                            if (response.isSuccessful) {
                                updatedPendingDeletionsDrivers.remove(dd)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                val updatedPendingDeletionsAssistants = pendingDeletionsAssistants.toMutableSet()
                for (da in pendingDeletionsAssistants) {
                    try {
                        val encodedKey = java.net.URLEncoder.encode("__CONFIG_ASSISTANT__$da", "UTF-8")
                        val deleteUrl = "$urlStr/destination/$encodedKey"
                        val request = okhttp3.Request.Builder().url(deleteUrl).delete().build()
                        client.newCall(request).execute().use { response ->
                            if (response.isSuccessful) {
                                updatedPendingDeletionsAssistants.remove(da)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                prefs.edit()
                    .putStringSet("pending_deletions_trips", updatedPendingDeletionsTrips)
                    .putStringSet("pending_deletions_vehicles", updatedPendingDeletionsVehicles)
                    .putStringSet("pending_deletions_drivers", updatedPendingDeletionsDrivers)
                    .putStringSet("pending_deletions_assistants", updatedPendingDeletionsAssistants)
                    .apply()

                // 4. Fetch all entries from SheetDB via GET
                val getRequest = okhttp3.Request.Builder()
                    .url(urlStr)
                    .get()
                    .build()

                client.newCall(getRequest).execute().use { response ->
                    if (!response.isSuccessful) {
                        launch(kotlinx.coroutines.Dispatchers.Main) {
                            isSyncing.value = false
                            if (response.code == 429) {
                                cooldownUntil = System.currentTimeMillis() + (4 * 60 * 1000) // 4 minutes cooldown
                                syncCooldownRemaining.value = 240L
                                val msg = if (currentLanguage.value == AppLanguage.SINHALA) {
                                    "ගූගල් ශීට් සීමාව ඉක්මවා ඇත (429). විනාඩි 4කට පසු ස්වයංක්‍රීයව ක්‍රියාත්මක වේ."
                                } else {
                                    "Google Sheets limit exceeded (429). Will auto-retry in 4 minutes."
                                }
                                syncErrorMessage.value = msg
                                if (isManual) {
                                    onResult(false, msg)
                                }
                            } else {
                                val msg = "HTTP Error: ${response.code}"
                                syncErrorMessage.value = msg
                                if (isManual) {
                                    onResult(false, msg)
                                }
                            }
                        }
                        return@use
                    }

                    val responseBodyStr = response.body?.string() ?: "[]"

                    // Parse response array of server entries
                    val serverArray = org.json.JSONArray(responseBodyStr)
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
                            if (plate.isNotBlank() && plate !in updatedPendingDeletionsVehicles) {
                                serverVehicles.add(VehicleSetting(plateNumber = plate, description = desc))
                            }
                        } else if (destinationVal.startsWith("__CONFIG_DRIVER__")) {
                            val name = destinationVal.removePrefix("__CONFIG_DRIVER__")
                            val phone = obj.optString("reason", "")
                            if (name.isNotBlank() && name !in updatedPendingDeletionsDrivers) {
                                serverDrivers.add(DriverSetting(name = name, phone = phone))
                            }
                        } else if (destinationVal.startsWith("__CONFIG_ASSISTANT__")) {
                            val name = destinationVal.removePrefix("__CONFIG_ASSISTANT__")
                            val phone = obj.optString("reason", "")
                            if (name.isNotBlank() && name !in updatedPendingDeletionsAssistants) {
                                serverAssistants.add(AssistantSetting(name = name, phone = phone))
                            }
                        } else {
                            val dateTimeMillisVal = obj.optLong("dateTimeMillis", 0L)
                            if (dateTimeMillisVal.toString() !in updatedPendingDeletionsTrips) {
                                val trip = TripLog(
                                    destination = destinationVal,
                                    reason = obj.optString("reason", ""),
                                    vehicleName = obj.optString("vehicleName", ""),
                                    driverName = obj.optString("driverName", ""),
                                    assistantName = obj.optString("assistantName", ""),
                                    distanceKm = obj.optDouble("distanceKm", 0.0),
                                    dateTimeMillis = dateTimeMillisVal,
                                    fuelOrderNumber = obj.optString("fuelOrderNumber", ""),
                                    fuelLiters = obj.optDouble("fuelLiters", 0.0)
                                )
                                serverTrips.add(trip)
                            }
                        }
                    }

                    val updatedPendingTrips = pendingTrips.toMutableSet()

                    // 5. Add server trips that are missing locally to local DB (ignoring those marked for deletion)
                    var newTripsAdded = 0
                    for (serverTrip in serverTrips) {
                        val exists = localTrips.any { local ->
                            local.destination.trim().equals(serverTrip.destination.trim(), ignoreCase = true) &&
                            local.vehicleName.trim().equals(serverTrip.vehicleName.trim(), ignoreCase = true) &&
                            local.driverName.trim().equals(serverTrip.driverName.trim(), ignoreCase = true) &&
                            local.dateTimeMillis == serverTrip.dateTimeMillis
                        }

                        if (!exists && serverTrip.dateTimeMillis.toString() !in updatedPendingDeletionsTrips) {
                            repository.insertTripLog(serverTrip)
                            newTripsAdded++
                        }
                    }

                    // 6. Find local trips that are missing on server and upload them via POST if pending
                    val tripsToUpload = mutableListOf<TripLog>()
                    for (localTrip in localTrips) {
                        val existsOnServer = serverTrips.any { serverTrip ->
                            localTrip.destination.trim().equals(serverTrip.destination.trim(), ignoreCase = true) &&
                            localTrip.vehicleName.trim().equals(serverTrip.vehicleName.trim(), ignoreCase = true) &&
                            localTrip.driverName.trim().equals(serverTrip.driverName.trim(), ignoreCase = true) &&
                            localTrip.dateTimeMillis == serverTrip.dateTimeMillis
                        }
                        if (!existsOnServer && localTrip.dateTimeMillis.toString() !in updatedPendingDeletionsTrips) {
                            val isPending = localTrip.dateTimeMillis.toString() in pendingTrips
                            if (isPending) {
                                tripsToUpload.add(localTrip)
                            } else {
                                // Already synced before but deleted on server, so delete locally as well
                                repository.deleteTripLogById(localTrip.id)
                            }
                        }
                    }

                    // 7. Gather configurations to upload
                    val configsToUpload = mutableListOf<org.json.JSONObject>()

                    val updatedPendingVehicles = pendingVehicles.toMutableSet()
                    for (lv in localVehicles) {
                        if (lv.plateNumber in updatedPendingDeletionsVehicles) continue
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
                            updatedPendingVehicles.remove(lv.plateNumber)
                        }
                    }

                    val updatedPendingDrivers = pendingDrivers.toMutableSet()
                    for (ld in localDrivers) {
                        if (ld.name in updatedPendingDeletionsDrivers) continue
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
                            updatedPendingDrivers.remove(ld.name)
                        }
                    }

                    val updatedPendingAssistants = pendingAssistants.toMutableSet()
                    for (la in localAssistants) {
                        if (la.name in updatedPendingDeletionsAssistants) continue
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
                            updatedPendingAssistants.remove(la.name)
                        }
                    }

                    val postArray = org.json.JSONArray()
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
                        postArray.put(obj)
                    }
                    for (config in configsToUpload) {
                        postArray.put(config)
                    }

                    if (postArray.length() > 0) {
                        val payload = org.json.JSONObject()
                        payload.put("data", postArray)

                        val requestBody = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                        val postRequest = okhttp3.Request.Builder()
                            .url(urlStr)
                            .post(requestBody)
                            .build()

                        client.newCall(postRequest).execute().use { postResponse ->
                            if (postResponse.isSuccessful) {
                                for (uploadedTrip in tripsToUpload) {
                                    updatedPendingTrips.remove(uploadedTrip.dateTimeMillis.toString())
                                }
                                prefs.edit()
                                    .putStringSet("pending_trips", updatedPendingTrips)
                                    .putStringSet("pending_vehicles", updatedPendingVehicles)
                                    .putStringSet("pending_drivers", updatedPendingDrivers)
                                    .putStringSet("pending_assistants", updatedPendingAssistants)
                                    .apply()
                            }
                        }
                    }

                    // 8. Align local databases with server configuration
                    for (sv in serverVehicles) {
                        val existsLocally = localVehicles.any { it.plateNumber == sv.plateNumber }
                        if (!existsLocally) {
                            repository.insertVehicle(sv)
                        }
                    }
                    for (lv in localVehicles) {
                        val existsOnServer = serverVehicles.any { it.plateNumber == lv.plateNumber }
                        val isPending = lv.plateNumber in pendingVehicles
                        val isDeleted = lv.plateNumber in updatedPendingDeletionsVehicles
                        if (!existsOnServer && !isPending && !isDeleted) {
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
                        val isDeleted = ld.name in updatedPendingDeletionsDrivers
                        if (!existsOnServer && !isPending && !isDeleted) {
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
                        val isDeleted = la.name in updatedPendingDeletionsAssistants
                        if (!existsOnServer && !isPending && !isDeleted) {
                            repository.deleteAssistantByName(la.name)
                        }
                    }

                    // Save sync time
                    val now = System.currentTimeMillis()
                    prefs.edit().putLong("last_sync_time", now).apply()
                    lastSyncTime.value = now

                    launch(kotlinx.coroutines.Dispatchers.Main) {
                        isSyncing.value = false
                        syncErrorMessage.value = null // clear error
                        val successMsg = if (currentLanguage.value == AppLanguage.SINHALA) {
                            "සාර්ථකව සමමුහුර්ත විය! නව ගමන්වාර $newTripsAdded ක් ලැබුණි."
                        } else {
                            "Synced successfully! Added $newTripsAdded new trip(s)."
                        }
                        onResult(true, successMsg)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                launch(kotlinx.coroutines.Dispatchers.Main) {
                    isSyncing.value = false
                    if (isManual) {
                        onResult(false, "Sync failed: ${e.message}")
                    }
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
                syncWithGoogleSheetsBackground()
            }
        }
    }

    fun updateTripLog(
        id: Int,
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
                id = id,
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
            repository.updateTripLog(log)
            
            // Mark trip as pending upload
            val pending = prefs.getStringSet("pending_trips", emptySet())?.toMutableSet() ?: mutableSetOf()
            pending.add(dateTimeMillis.toString())
            prefs.edit().putStringSet("pending_trips", pending).apply()
            
            // Trigger auto sync in background if URL is set
            if (syncUrl.value.isNotBlank()) {
                syncWithGoogleSheetsBackground()
            }
        }
    }

    fun deleteTripLog(id: Int) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val tripLog = repository.allTripLogs.first().find { it.id == id }
            repository.deleteTripLogById(id)
            
            if (tripLog != null) {
                // Add to pending deletions to prevent resurrection
                val pendingDeletions = prefs.getStringSet("pending_deletions_trips", emptySet())?.toMutableSet() ?: mutableSetOf()
                pendingDeletions.add(tripLog.dateTimeMillis.toString())
                prefs.edit().putStringSet("pending_deletions_trips", pendingDeletions).apply()

                // Remove from pending trips if any
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
                                // Deleted from server successfully, remove from pending deletions
                                val updatedPendingDeletions = prefs.getStringSet("pending_deletions_trips", emptySet())?.toMutableSet() ?: mutableSetOf()
                                if (updatedPendingDeletions.remove(tripLog.dateTimeMillis.toString())) {
                                    prefs.edit().putStringSet("pending_deletions_trips", updatedPendingDeletions).apply()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                // Sync immediately to update status in background
                syncWithGoogleSheetsBackground()
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

    private fun deleteConfigFromServer(destinationKey: String, deletionPrefKey: String? = null, deletionItemKey: String? = null) {
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
                        // Deleted successfully, remove from pending deletions
                        if (deletionPrefKey != null && deletionItemKey != null) {
                            val pendingDeletions = prefs.getStringSet(deletionPrefKey, emptySet())?.toMutableSet() ?: mutableSetOf()
                            if (pendingDeletions.remove(deletionItemKey)) {
                                prefs.edit().putStringSet(deletionPrefKey, pendingDeletions).apply()
                            }
                        }
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
            syncWithGoogleSheetsBackground()
        }
    }

    fun deleteVehicle(id: Int, plateNumber: String) {
        viewModelScope.launch {
            repository.deleteVehicleById(id)
            val pending = prefs.getStringSet("pending_vehicles", emptySet())?.toMutableSet() ?: mutableSetOf()
            if (pending.remove(plateNumber)) {
                prefs.edit().putStringSet("pending_vehicles", pending).apply()
            }
            
            // Add to pending deletions to prevent resurrection
            val pendingDeletions = prefs.getStringSet("pending_deletions_vehicles", emptySet())?.toMutableSet() ?: mutableSetOf()
            pendingDeletions.add(plateNumber)
            prefs.edit().putStringSet("pending_deletions_vehicles", pendingDeletions).apply()
            
            deleteConfigFromServer("__CONFIG_VEHICLE__$plateNumber", "pending_deletions_vehicles", plateNumber)
        }
    }

    // Driver Configuration Methods
    fun addDriver(name: String, phone: String) {
        viewModelScope.launch {
            repository.insertDriver(DriverSetting(name = name, phone = phone))
            val pending = prefs.getStringSet("pending_drivers", emptySet())?.toMutableSet() ?: mutableSetOf()
            pending.add(name)
            prefs.edit().putStringSet("pending_drivers", pending).apply()
            syncWithGoogleSheetsBackground()
        }
    }

    fun deleteDriver(id: Int, name: String) {
        viewModelScope.launch {
            repository.deleteDriverById(id)
            val pending = prefs.getStringSet("pending_drivers", emptySet())?.toMutableSet() ?: mutableSetOf()
            if (pending.remove(name)) {
                prefs.edit().putStringSet("pending_drivers", pending).apply()
            }
            
            // Add to pending deletions to prevent resurrection
            val pendingDeletions = prefs.getStringSet("pending_deletions_drivers", emptySet())?.toMutableSet() ?: mutableSetOf()
            pendingDeletions.add(name)
            prefs.edit().putStringSet("pending_deletions_drivers", pendingDeletions).apply()
            
            deleteConfigFromServer("__CONFIG_DRIVER__$name", "pending_deletions_drivers", name)
        }
    }

    // Assistant Configuration Methods
    fun addAssistant(name: String, phone: String) {
        viewModelScope.launch {
            repository.insertAssistant(AssistantSetting(name = name, phone = phone))
            val pending = prefs.getStringSet("pending_assistants", emptySet())?.toMutableSet() ?: mutableSetOf()
            pending.add(name)
            prefs.edit().putStringSet("pending_assistants", pending).apply()
            syncWithGoogleSheetsBackground()
        }
    }

    fun deleteAssistant(id: Int, name: String) {
        viewModelScope.launch {
            repository.deleteAssistantById(id)
            val pending = prefs.getStringSet("pending_assistants", emptySet())?.toMutableSet() ?: mutableSetOf()
            if (pending.remove(name)) {
                prefs.edit().putStringSet("pending_assistants", pending).apply()
            }
            
            // Add to pending deletions to prevent resurrection
            val pendingDeletions = prefs.getStringSet("pending_deletions_assistants", emptySet())?.toMutableSet() ?: mutableSetOf()
            pendingDeletions.add(name)
            prefs.edit().putStringSet("pending_deletions_assistants", pendingDeletions).apply()
            
            deleteConfigFromServer("__CONFIG_ASSISTANT__$name", "pending_deletions_assistants", name)
        }
    }

    fun insertOdometerCalculation(
        vehicleName: String,
        initialOdometer: Double,
        fuelEfficiency: Double,
        fuelConsumed: Double,
        initialFuel: Double,
        fuelObtained: Double,
        finalOdometer: Double,
        remainingFuel: Double
    ) {
        viewModelScope.launch {
            val calc = OdometerCalculation(
                vehicleName = vehicleName,
                initialOdometer = initialOdometer,
                fuelEfficiency = fuelEfficiency,
                fuelConsumed = fuelConsumed,
                initialFuel = initialFuel,
                fuelObtained = fuelObtained,
                finalOdometer = finalOdometer,
                remainingFuel = remainingFuel,
                dateTimeMillis = System.currentTimeMillis()
            )
            repository.insertOdometerCalculation(calc)
        }
    }

    fun deleteOdometerCalculation(id: Int) {
        viewModelScope.launch {
            repository.deleteOdometerCalculationById(id)
        }
    }

    fun clearAllOdometerCalculations() {
        viewModelScope.launch {
            repository.deleteAllOdometerCalculations()
        }
    }
}

