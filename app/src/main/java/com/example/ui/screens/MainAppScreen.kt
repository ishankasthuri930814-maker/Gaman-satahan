package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PlannedTrip
import com.example.data.model.TripLog
import com.example.ui.translation.AppLanguage
import com.example.ui.translation.Translations
import com.example.ui.viewmodel.TripViewModel
import java.text.SimpleDateFormat
import java.util.*

enum class AppTab {
    TRIPS,
    REMINIDERS,
    ANALYTICS,
    SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: TripViewModel) {
    val context = LocalContext.current
    val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(AppTab.TRIPS) }

    // Alarm/Notification permission checking
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            Toast.makeText(
                context,
                if (currentLang == AppLanguage.SINHALA) "දැනුම්දීම් සක්‍රිය කරන ලදී!" else "Notifications enabled!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                context,
                if (currentLang == AppLanguage.SINHALA) "දැනුම්දීම් අවසරය ප්‍රතික්ෂේප කරන ලදී." else "Notification permission denied.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = Translations.getString("app_title", currentLang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = if (currentLang == AppLanguage.SINHALA) "දිනපතා වියදම් සහ ගමන් සටහන්" else "Daily Expense & Trip Ledger",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    // Quick Language Switcher Button
                    Button(
                        onClick = {
                            val nextLang = if (currentLang == AppLanguage.SINHALA) AppLanguage.ENGLISH else AppLanguage.SINHALA
                            viewModel.setLanguage(nextLang)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("language_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Switch Language",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentLang == AppLanguage.SINHALA) "ENGLISH" else "සිංහල",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == AppTab.TRIPS,
                    onClick = { selectedTab = AppTab.TRIPS },
                    icon = { Icon(Icons.Default.DirectionsCar, contentDescription = "Trip Logs") },
                    label = { Text(Translations.getString("trips_tab", currentLang)) },
                    modifier = Modifier.testTag("trips_tab_button")
                )
                NavigationBarItem(
                    selected = selectedTab == AppTab.REMINIDERS,
                    onClick = { selectedTab = AppTab.REMINIDERS },
                    icon = { Icon(Icons.Default.NotificationsActive, contentDescription = "Reminders") },
                    label = { Text(Translations.getString("planner_tab", currentLang)) },
                    modifier = Modifier.testTag("reminders_tab_button")
                )
                NavigationBarItem(
                    selected = selectedTab == AppTab.ANALYTICS,
                    onClick = { selectedTab = AppTab.ANALYTICS },
                    icon = { Icon(Icons.Default.Leaderboard, contentDescription = "Analytics") },
                    label = { Text(Translations.getString("stats_tab", currentLang)) },
                    modifier = Modifier.testTag("analytics_tab_button")
                )
                NavigationBarItem(
                    selected = selectedTab == AppTab.SETTINGS,
                    onClick = { selectedTab = AppTab.SETTINGS },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text(Translations.getString("settings_tab", currentLang)) },
                    modifier = Modifier.testTag("settings_tab_button")
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (selectedTab) {
                AppTab.TRIPS -> {
                    TripLogsScreen(viewModel = viewModel, currentLang = currentLang)
                }
                AppTab.REMINIDERS -> {
                    RemindersScreen(
                        viewModel = viewModel,
                        currentLang = currentLang,
                        hasPermission = hasNotificationPermission,
                        onRequestPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    )
                }
                AppTab.ANALYTICS -> {
                    AnalyticsScreen(viewModel = viewModel, currentLang = currentLang)
                }
                AppTab.SETTINGS -> {
                    SettingsScreen(viewModel = viewModel, currentLang = currentLang)
                }
            }
        }
    }
}

// ==========================================
// 1. TRIP LOGS SCREEN & DIALOGS
// ==========================================

@Composable
fun TripLogsScreen(viewModel: TripViewModel, currentLang: AppLanguage) {
    val trips by viewModel.tripLogs.collectAsStateWithLifecycle()
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
    val drivers by viewModel.drivers.collectAsStateWithLifecycle()
    val assistants by viewModel.assistants.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    // Sync state flows
    val syncUrl by viewModel.syncUrl.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.lastSyncTime.collectAsStateWithLifecycle()

    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. CLOUD SYNC CARD
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.Cloud,
                                    contentDescription = null,
                                    tint = if (syncUrl.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = Translations.getString("sync_title", currentLang),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    if (syncUrl.isBlank()) {
                                        Text(
                                            text = if (currentLang == AppLanguage.SINHALA) "සමකාලීන කිරීම සකසා නැත (සැකසුම් බලන්න)" else "Not configured (Check Settings)",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    } else {
                                        val timeStr = if (lastSyncTime == 0L) {
                                            if (currentLang == AppLanguage.SINHALA) "කිසිදිනක නැත" else "Never"
                                        } else {
                                            SimpleDateFormat("hh:mm a (MMM dd)", Locale.getDefault()).format(Date(lastSyncTime))
                                        }
                                        Text(
                                            text = "${Translations.getString("last_synced", currentLang)}: $timeStr",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            if (syncUrl.isNotBlank()) {
                                Button(
                                    onClick = {
                                        viewModel.syncWithGoogleSheets { success, msg ->
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    enabled = !isSyncing,
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    if (isSyncing) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(Translations.getString("syncing", currentLang), fontSize = if (currentLang == AppLanguage.SINHALA) 10.sp else 12.sp)
                                    } else {
                                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(Translations.getString("sync_button", currentLang), fontSize = if (currentLang == AppLanguage.SINHALA) 10.sp else 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. TRIP HISTORY HEADER
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (currentLang == AppLanguage.SINHALA) "පසුගිය ගමන් සටහන්" else "Recent Trip History",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            // 4. RECENT TRIPS LIST OR EMPTY STATE
            if (trips.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (currentLang == AppLanguage.SINHALA) "ගමන් විස්තර නොමැත" else "No Trips Recorded",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = Translations.getString("no_trips", currentLang),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(trips, key = { it.id }) { trip ->
                    TripLogCard(trip = trip, currentLang = currentLang, onDelete = {
                        viewModel.deleteTripLog(trip.id)
                    })
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp)) // Leave space for FAB
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_trip_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Trip")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Translations.getString("add_trip", currentLang),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (showAddDialog) {
            AddTripLogDialog(
                currentLang = currentLang,
                vehicles = vehicles,
                drivers = drivers,
                assistants = assistants,
                onDismiss = { showAddDialog = false },
                onSave = { destination, reason, vehicle, driver, assistant, distance, timestamp, fuelOrder, liters ->
                    // Auto-add new custom values to Settings/Google Sheet Configuration
                    if (vehicle.isNotBlank() && vehicles.none { it.plateNumber.trim().equals(vehicle.trim(), ignoreCase = true) }) {
                        viewModel.addVehicle(vehicle.trim(), "")
                    }
                    if (driver.isNotBlank() && drivers.none { it.name.trim().equals(driver.trim(), ignoreCase = true) }) {
                        viewModel.addDriver(driver.trim(), "")
                    }
                    val noAssistantTextSinhala = "හෙල්පර් කෙනෙක් නැත"
                    val noAssistantTextEnglish = "No Assistant"
                    if (assistant.isNotBlank() && 
                        assistant != noAssistantTextSinhala && 
                        assistant != noAssistantTextEnglish &&
                        assistants.none { it.name.trim().equals(assistant.trim(), ignoreCase = true) }
                    ) {
                        viewModel.addAssistant(assistant.trim(), "")
                    }

                    viewModel.addTripLog(
                        destination = destination,
                        reason = reason,
                        vehicleName = vehicle,
                        driverName = driver,
                        assistantName = assistant,
                        distanceKm = distance,
                        dateTimeMillis = timestamp,
                        fuelOrderNumber = fuelOrder,
                        fuelLiters = liters
                    )
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun TripLogCard(trip: TripLog, currentLang: AppLanguage, onDelete: () -> Unit) {
    val sdf = SimpleDateFormat("yyyy MMM dd - hh:mm a", Locale.getDefault())
    val formattedDate = sdf.format(Date(trip.dateTimeMillis))
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var enteredPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
            )
            .testTag("trip_card_${trip.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Destination & Distance Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Place",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = trip.destination,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = trip.reason,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 24.dp, top = 2.dp)
                    )
                }

                // Distance Badge
                Box(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            ),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${trip.distanceKm} km",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )

            // Vehicle and Crew Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Vehicle
                Column {
                    LabelText(if (currentLang == AppLanguage.SINHALA) "වාහනය" else "Vehicle")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (trip.vehicleName == "Vehicle A") {
                                Translations.getString("vehicle_a", currentLang)
                            } else {
                                Translations.getString("vehicle_b", currentLang)
                            },
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }

                // Driver
                Column {
                    LabelText(Translations.getString("driver", currentLang))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = trip.driverName.ifBlank { "-" },
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }

                // Assistant
                Column {
                    LabelText(Translations.getString("assistant", currentLang))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = trip.assistantName.ifBlank { "-" },
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            if (trip.fuelOrderNumber.isNotBlank() || trip.fuelLiters > 0.0) {
                Divider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (trip.fuelOrderNumber.isNotBlank()) {
                        Column(modifier = Modifier.weight(1f)) {
                            LabelText(if (currentLang == AppLanguage.SINHALA) "ඉන්ධන ඇනවුම් අංකය" else "Fuel Order No.")
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ConfirmationNumber,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = trip.fuelOrderNumber,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    if (trip.fuelLiters > 0.0) {
                        Column(modifier = Modifier.weight(1f)) {
                            LabelText(if (currentLang == AppLanguage.SINHALA) "ඉන්ධන ලීටර් ප්‍රමාණය" else "Fuel Liters")
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalGasStation,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${trip.fuelLiters} L",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )

            // Footer: Date and Delete Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formattedDate,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirm = false
                enteredPassword = ""
                passwordError = false
            },
            title = { Text(Translations.getString("delete_confirm", currentLang)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = Translations.getString("enter_password", currentLang),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = enteredPassword,
                        onValueChange = {
                            enteredPassword = it
                            passwordError = false
                        },
                        label = { Text(Translations.getString("password_label", currentLang)) },
                        modifier = Modifier.fillMaxWidth().testTag("delete_password_input_field"),
                        singleLine = true,
                        isError = passwordError,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                    if (passwordError) {
                        Text(
                            text = Translations.getString("password_incorrect", currentLang),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (enteredPassword == "Ishan1993") {
                            onDelete()
                            showDeleteConfirm = false
                            enteredPassword = ""
                            passwordError = false
                        } else {
                            passwordError = true
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(Translations.getString("delete", currentLang))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        enteredPassword = ""
                        passwordError = false
                    }
                ) {
                    Text(Translations.getString("cancel", currentLang))
                }
            }
        )
    }
}

@Composable
fun LabelText(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Light,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )
}

@Composable
fun SettingsDropdownSelector(
    label: String,
    selectedValue: String,
    options: List<String>,
    onValueSelected: (String) -> Unit,
    placeholder: String = "",
    testTag: String = ""
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .testTag(testTag),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.outlinedCardColors()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = selectedValue.ifBlank { placeholder },
                        fontSize = 15.sp,
                        color = if (selectedValue.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Normal
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            if (options.isEmpty()) {
                DropdownMenuItem(
                    text = { Text(text = "No options. Add in Settings tab!", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic) },
                    onClick = { expanded = false }
                )
            } else {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option, fontWeight = FontWeight.Medium) },
                        onClick = {
                            onValueSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTripLogDialog(
    currentLang: AppLanguage,
    vehicles: List<com.example.data.model.VehicleSetting>,
    drivers: List<com.example.data.model.DriverSetting>,
    assistants: List<com.example.data.model.AssistantSetting>,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, Double, Long, String, Double) -> Unit
) {
    val context = LocalContext.current
    var destination by remember { mutableStateOf("") }
    val additionalStops = remember { mutableStateListOf<String>() }
    var reason by remember { mutableStateOf("") }
    var showIncompleteDialog by remember { mutableStateOf(false) }

    if (showIncompleteDialog) {
        AlertDialog(
            onDismissRequest = { showIncompleteDialog = false },
            title = {
                Text(
                    text = Translations.getString("incomplete_details", currentLang),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = if (currentLang == AppLanguage.SINHALA) {
                        "කරුණාකර සියලුම විස්තර (යන ස්ථානය, හේතුව, වාහනය, රියදුරු, හෙල්පර් සහ දුර) නිවැරදිව ඇතුළත් කරන්න."
                    } else {
                        "Please fill in all details (destination, reason, vehicle, driver, assistant, and distance) correctly."
                    },
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showIncompleteDialog = false },
                    modifier = Modifier.testTag("close_incomplete_details_btn")
                ) {
                    Text(Translations.getString("close", currentLang))
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
    
    // Vehicle selection
    var selectedVehicle by remember { mutableStateOf("") }
    var customVehicle by remember { mutableStateOf("") }
    var isCustomVehicleExpanded by remember { mutableStateOf(false) }

    // Driver selection
    var selectedDriver by remember { mutableStateOf("") }
    var customDriver by remember { mutableStateOf("") }
    var isCustomDriverExpanded by remember { mutableStateOf(false) }

    // Assistant selection
    var selectedAssistant by remember { mutableStateOf("") }
    var customAssistant by remember { mutableStateOf("") }
    var isCustomAssistantExpanded by remember { mutableStateOf(false) }

    var distanceStr by remember { mutableStateOf("") }
    var fuelOrderNumber by remember { mutableStateOf("") }
    var fuelLitersStr by remember { mutableStateOf("") }

    // Date/Time States
    val calendar = remember { Calendar.getInstance() }
    var selectedDateTime by remember { mutableStateOf(calendar.timeInMillis) }
    var selectedDateOption by remember { mutableStateOf("today") }
    val sdf = SimpleDateFormat("yyyy MMM dd - hh:mm a", Locale.getDefault())

    // Automatic pre-selection for standard values
    LaunchedEffect(vehicles, drivers, assistants) {
        if (selectedVehicle.isEmpty() && vehicles.isNotEmpty()) {
            selectedVehicle = vehicles.first().plateNumber
        }
        if (selectedDriver.isEmpty() && drivers.isNotEmpty()) {
            selectedDriver = drivers.first().name
        }
        if (selectedAssistant.isEmpty() && assistants.isNotEmpty()) {
            selectedAssistant = assistants.first().name
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = Translations.getString("add_trip", currentLang),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    // Date & Time Selector Button
                    Text(
                        text = if (currentLang == AppLanguage.SINHALA) "ගමන සිදුවූ දිනය" else "Trip Date",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val options = listOf(
                            Triple("today", if (currentLang == AppLanguage.SINHALA) "අද" else "Today", 0),
                            Triple("yesterday", if (currentLang == AppLanguage.SINHALA) "ඊයේ" else "Yesterday", -1),
                            Triple("day_before", if (currentLang == AppLanguage.SINHALA) "පෙරේදා" else "Day Before", -2),
                            Triple("custom", if (currentLang == AppLanguage.SINHALA) "වෙනත්" else "Custom", null)
                        )
                        
                        options.forEach { (optionKey, optionLabel, dayOffset) ->
                            val isSelected = selectedDateOption == optionKey
                            val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            val borderStrokeModifier = if (isSelected) Modifier else Modifier.border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = containerColor,
                                contentColor = contentColor,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .then(borderStrokeModifier)
                                    .clickable {
                                        selectedDateOption = optionKey
                                        if (dayOffset != null) {
                                            val tempCal = Calendar.getInstance()
                                            tempCal.add(Calendar.DAY_OF_YEAR, dayOffset)
                                            val hour = calendar.get(Calendar.HOUR_OF_DAY)
                                            val minute = calendar.get(Calendar.MINUTE)
                                            calendar.timeInMillis = tempCal.timeInMillis
                                            calendar.set(Calendar.HOUR_OF_DAY, hour)
                                            calendar.set(Calendar.MINUTE, minute)
                                            calendar.set(Calendar.SECOND, 0)
                                            selectedDateTime = calendar.timeInMillis
                                        } else {
                                            // Open Android Native DatePickerDialog
                                            val datePicker = DatePickerDialog(
                                                context,
                                                { _, year, month, day ->
                                                    calendar.set(Calendar.YEAR, year)
                                                    calendar.set(Calendar.MONTH, month)
                                                    calendar.set(Calendar.DAY_OF_MONTH, day)
            
                                                    // Open TimePickerDialog immediately after
                                                    val timePicker = TimePickerDialog(
                                                        context,
                                                        { _, hour, minute ->
                                                            calendar.set(Calendar.HOUR_OF_DAY, hour)
                                                            calendar.set(Calendar.MINUTE, minute)
                                                            calendar.set(Calendar.SECOND, 0)
                                                            selectedDateTime = calendar.timeInMillis
                                                        },
                                                        calendar.get(Calendar.HOUR_OF_DAY),
                                                        calendar.get(Calendar.MINUTE),
                                                        false
                                                    )
                                                    timePicker.show()
                                                },
                                                calendar.get(Calendar.YEAR),
                                                calendar.get(Calendar.MONTH),
                                                calendar.get(Calendar.DAY_OF_MONTH)
                                            )
                                            datePicker.show()
                                        }
                                    }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = optionLabel,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedDateOption = "custom"
                                // Open Android Native DatePickerDialog
                                val datePicker = DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        calendar.set(Calendar.YEAR, year)
                                        calendar.set(Calendar.MONTH, month)
                                        calendar.set(Calendar.DAY_OF_MONTH, day)
                                        
                                        // Open TimePickerDialog immediately after
                                        val timePicker = TimePickerDialog(
                                            context,
                                            { _, hour, minute ->
                                                calendar.set(Calendar.HOUR_OF_DAY, hour)
                                                calendar.set(Calendar.MINUTE, minute)
                                                calendar.set(Calendar.SECOND, 0)
                                                selectedDateTime = calendar.timeInMillis
                                            },
                                            calendar.get(Calendar.HOUR_OF_DAY),
                                            calendar.get(Calendar.MINUTE),
                                            false
                                        )
                                        timePicker.show()
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                )
                                datePicker.show()
                            }
                            .testTag("input_date_time_selector")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = sdf.format(Date(selectedDateTime)),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                                contentDescription = "Edit Date Time",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                item {
                    // Place Name Input
                    OutlinedTextField(
                        value = destination,
                        onValueChange = { destination = it },
                        label = { Text(Translations.getString("first_destination_label", currentLang)) },
                        placeholder = { Text(Translations.getString("destination_placeholder", currentLang)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_destination")
                    )
                }

                item {
                    // Additional stops dynamic layout
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (index in additionalStops.indices) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = additionalStops[index],
                                    onValueChange = { additionalStops[index] = it },
                                    label = { Text("${Translations.getString("stop_label", currentLang)} ${index + 2}") },
                                    placeholder = { Text(Translations.getString("destination_placeholder", currentLang)) },
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { additionalStops.removeAt(index) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Stop", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        TextButton(
                            onClick = { additionalStops.add("") },
                            modifier = Modifier.align(Alignment.Start)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(Translations.getString("add_stop", currentLang))
                        }
                    }
                }

                item {
                    // Reason/Purpose Input
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text(Translations.getString("reason", currentLang)) },
                        placeholder = { Text(Translations.getString("reason_placeholder", currentLang)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_reason")
                    )
                }

                item {
                    // Vehicle Selection Dropdown
                    val customPrompt = if (currentLang == AppLanguage.SINHALA) "+ අලුත් වාහනයක්..." else "+ Enter Custom Vehicle..."
                    val vehicleOptions = vehicles.map { it.plateNumber } + customPrompt
                    
                    SettingsDropdownSelector(
                        label = Translations.getString("vehicle", currentLang),
                        selectedValue = selectedVehicle,
                        options = vehicleOptions,
                        onValueSelected = { value ->
                            selectedVehicle = value
                            isCustomVehicleExpanded = (value == customPrompt)
                        },
                        placeholder = if (currentLang == AppLanguage.SINHALA) "වාහනයක් තෝරන්න" else "Select a Vehicle",
                        testTag = "input_vehicle_dropdown"
                    )

                    if (isCustomVehicleExpanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customVehicle,
                            onValueChange = { customVehicle = it },
                            label = { Text(if (currentLang == AppLanguage.SINHALA) "අලුත් වාහන අංකය" else "Custom Vehicle Number") },
                            placeholder = { Text("e.g. WP CAB-1234") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_custom_vehicle")
                        )
                    }
                }

                item {
                    // Driver Selection Dropdown
                    val customPrompt = if (currentLang == AppLanguage.SINHALA) "+ අලුත් රියදුරෙක්..." else "+ Enter Custom Driver..."
                    val driverOptions = drivers.map { it.name } + customPrompt

                    SettingsDropdownSelector(
                        label = Translations.getString("driver", currentLang),
                        selectedValue = selectedDriver,
                        options = driverOptions,
                        onValueSelected = { value ->
                            selectedDriver = value
                            isCustomDriverExpanded = (value == customPrompt)
                        },
                        placeholder = if (currentLang == AppLanguage.SINHALA) "රියදුරෙකු තෝරන්න" else "Select a Driver",
                        testTag = "input_driver_dropdown"
                    )

                    if (isCustomDriverExpanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customDriver,
                            onValueChange = { customDriver = it },
                            label = { Text(if (currentLang == AppLanguage.SINHALA) "අලුත් රියදුරු නම" else "Custom Driver Name") },
                            placeholder = { Text(Translations.getString("driver_placeholder", currentLang)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_custom_driver")
                        )
                    }
                }

                item {
                    // Assistant Selection Dropdown
                    val customPrompt = if (currentLang == AppLanguage.SINHALA) "+ අලුත් හෙල්පර් කෙනෙක්..." else "+ Enter Custom Assistant..."
                    val assistantOptions = assistants.map { it.name } + customPrompt

                    SettingsDropdownSelector(
                        label = Translations.getString("assistant", currentLang),
                        selectedValue = selectedAssistant,
                        options = assistantOptions,
                        onValueSelected = { value ->
                            selectedAssistant = value
                            isCustomAssistantExpanded = (value == customPrompt)
                        },
                        placeholder = if (currentLang == AppLanguage.SINHALA) "හෙල්පර් කෙනෙක් තෝරන්න" else "Select an Assistant",
                        testTag = "input_assistant_dropdown"
                    )

                    if (isCustomAssistantExpanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customAssistant,
                            onValueChange = { customAssistant = it },
                            label = { Text(if (currentLang == AppLanguage.SINHALA) "අලුත් හෙල්පර්ගේ නම" else "Custom Assistant Name") },
                            placeholder = { Text(Translations.getString("assistant_placeholder", currentLang)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_custom_assistant")
                        )
                    }
                }

                item {
                    // Distance KM Input
                    OutlinedTextField(
                        value = distanceStr,
                        onValueChange = { distanceStr = it },
                        label = { Text(Translations.getString("distance", currentLang)) },
                        placeholder = { Text(Translations.getString("distance_placeholder", currentLang)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_distance")
                    )
                }

                item {
                    // Fuel Order Number Input
                    OutlinedTextField(
                        value = fuelOrderNumber,
                        onValueChange = { fuelOrderNumber = it },
                        label = { Text(Translations.getString("fuel_order_number", currentLang)) },
                        placeholder = { Text(Translations.getString("fuel_order_placeholder", currentLang)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_fuel_order_number"),
                        singleLine = true
                    )
                }

                item {
                    // Fuel Liters Input
                    OutlinedTextField(
                        value = fuelLitersStr,
                        onValueChange = { fuelLitersStr = it },
                        label = { Text(Translations.getString("fuel_liters", currentLang)) },
                        placeholder = { Text(Translations.getString("fuel_liters_placeholder", currentLang)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_fuel_liters"),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalVehicle = if (isCustomVehicleExpanded) customVehicle else selectedVehicle
                    val finalDriver = if (isCustomDriverExpanded) customDriver else selectedDriver
                    val finalAssistant = if (isCustomAssistantExpanded) customAssistant else selectedAssistant

                    if (destination.isBlank() ||
                        reason.isBlank() ||
                        finalVehicle.isBlank() || finalVehicle.startsWith("+") ||
                        finalDriver.isBlank() || finalDriver.startsWith("+") ||
                        finalAssistant.isBlank() || finalAssistant.startsWith("+") ||
                        distanceStr.isBlank() || distanceStr.toDoubleOrNull() == null
                    ) {
                        showIncompleteDialog = true
                        return@Button
                    }

                    val finalDestination = if (additionalStops.isEmpty()) {
                        destination
                    } else {
                        val activeStops = additionalStops.filter { it.isNotBlank() }
                        if (activeStops.isEmpty()) {
                            destination
                        } else {
                            "$destination ➔ " + activeStops.joinToString(" ➔ ")
                        }
                    }

                    val distance = distanceStr.toDoubleOrNull() ?: 0.0
                    val fuelLiters = fuelLitersStr.toDoubleOrNull() ?: 0.0
                    onSave(
                        finalDestination,
                        reason,
                        finalVehicle,
                        finalDriver,
                        finalAssistant,
                        distance,
                        selectedDateTime,
                        fuelOrderNumber.trim(),
                        fuelLiters
                    )
                },
                modifier = Modifier.testTag("save_trip_button")
            ) {
                Text(Translations.getString("save", currentLang))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Translations.getString("cancel", currentLang))
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

// ==========================================
// 2. REMINDERS & ALARMS SCREEN & DIALOGS
// ==========================================

@Composable
fun RemindersScreen(
    viewModel: TripViewModel,
    currentLang: AppLanguage,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    val plannedTrips by viewModel.plannedTrips.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Notification Permission Notice Box
            if (!hasPermission) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Permission Alert",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (currentLang == AppLanguage.SINHALA) "නොටිෆිකේෂන් ඔන් කරන්න!" else "Notification Permission Required",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = Translations.getString("notification_permission_required", currentLang),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onRequestPermission,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Text(Translations.getString("grant_permission", currentLang))
                        }
                    }
                }
            }

            // Explanatory info box about alarms
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = "Alarms Info",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = Translations.getString("alarm_intervals", currentLang),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 16.sp
                    )
                }
            }

            if (plannedTrips.isEmpty()) {
                Box(modifier = Modifier.weight(1f)) {
                    EmptyState(
                        icon = Icons.Default.AddAlarm,
                        title = if (currentLang == AppLanguage.SINHALA) "එලාම් මොකුත් සෙට් කරලා නැහැ" else "No Alarms Scheduled",
                        description = Translations.getString("no_planned", currentLang)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = Translations.getString("alarms_header", currentLang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    items(plannedTrips, key = { it.id }) { planned ->
                        PlannedTripCard(planned = planned, currentLang = currentLang, onDelete = {
                            viewModel.deletePlannedTrip(planned.id)
                        })
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("plan_trip_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.NotificationAdd, contentDescription = "Add Alarm")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Translations.getString("add_planned", currentLang),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (showAddDialog) {
            AddPlannedTripDialog(
                currentLang = currentLang,
                onDismiss = { showAddDialog = false },
                onSave = { title, destination, notes, scheduledTime ->
                    viewModel.addPlannedTrip(title, destination, notes, scheduledTime)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun PlannedTripCard(planned: PlannedTrip, currentLang: AppLanguage, onDelete: () -> Unit) {
    val sdf = SimpleDateFormat("yyyy MMM dd - hh:mm a", Locale.getDefault())
    val formattedDate = sdf.format(Date(planned.scheduledTimeMillis))
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val now = System.currentTimeMillis()
    val isPast = planned.scheduledTimeMillis < now

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPast) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isPast) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
                RoundedCornerShape(16.dp)
            )
            .testTag("planned_card_${planned.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title & Time Count text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = planned.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (isPast) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Place",
                            tint = if (isPast) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = planned.destination,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = if (isPast) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status tag
                Box(
                    modifier = Modifier
                        .background(
                            if (isPast) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.tertiaryContainer,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isPast) {
                            if (currentLang == AppLanguage.SINHALA) "පරණ" else "Past"
                        } else {
                            if (currentLang == AppLanguage.SINHALA) "ක්‍රියාත්මක" else "Active"
                        },
                        color = if (isPast) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onTertiaryContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            if (planned.notes.isNotBlank()) {
                Text(
                    text = planned.notes,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )

            // Reminder schedule status badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = if (isPast) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (currentLang == AppLanguage.SINHALA) "එලාම් වදින්නේ:" else "Alarms active:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )

                // 24h milestone badge
                AlarmMilestoneBadge(
                    text = if (currentLang == AppLanguage.SINHALA) "දින 1කට කලින්" else "1d",
                    isActive = !isPast && (planned.scheduledTimeMillis - 24 * 3600 * 1000 > now)
                )
                // 3h milestone badge
                AlarmMilestoneBadge(
                    text = if (currentLang == AppLanguage.SINHALA) "පැය 3කට කලින්" else "3h",
                    isActive = !isPast && (planned.scheduledTimeMillis - 3 * 3600 * 1000 > now)
                )
                // 1h milestone badge
                AlarmMilestoneBadge(
                    text = if (currentLang == AppLanguage.SINHALA) "පැයකට කලින්" else "1h",
                    isActive = !isPast && (planned.scheduledTimeMillis - 1 * 3600 * 1000 > now)
                )
            }

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )

            // Footer: Date and Delete Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formattedDate,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Planned Trip",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(Translations.getString("delete_confirm", currentLang)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(Translations.getString("delete", currentLang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(Translations.getString("cancel", currentLang))
                }
            }
        )
    }
}

@Composable
fun AlarmMilestoneBadge(text: String, isActive: Boolean) {
    Box(
        modifier = Modifier
            .background(
                if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlannedTripDialog(
    currentLang: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Long) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Date/Time States (Needs to be in the future)
    val calendar = remember {
        Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 2) // Default to 2 hours in the future
        }
    }
    var selectedDateTime by remember { mutableStateOf(calendar.timeInMillis) }
    val sdf = SimpleDateFormat("yyyy MMM dd - hh:mm a", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = Translations.getString("add_planned", currentLang),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    // Title (e.g. Special Meeting)
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(if (currentLang == AppLanguage.SINHALA) "යන හේතුව" else "Meeting / Trip Title") },
                        placeholder = { Text(Translations.getString("reason_placeholder", currentLang)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("planned_input_title")
                    )
                }

                item {
                    // Location / Destination
                    OutlinedTextField(
                        value = destination,
                        onValueChange = { destination = it },
                        label = { Text(Translations.getString("destination", currentLang)) },
                        placeholder = { Text(Translations.getString("destination_placeholder", currentLang)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("planned_input_destination")
                    )
                }

                item {
                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(if (currentLang == AppLanguage.SINHALA) "අමතර විස්තර" else "Special Notes / Details") },
                        placeholder = { Text(if (currentLang == AppLanguage.SINHALA) "උදා: බඩු ගෙනියන්න" else "e.g. Bring files, call client") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("planned_input_notes")
                    )
                }

                item {
                    // Date & Time Selector Button
                    Text(
                        text = Translations.getString("date_time", currentLang),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Date Picker
                                val datePicker = DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        calendar.set(Calendar.YEAR, year)
                                        calendar.set(Calendar.MONTH, month)
                                        calendar.set(Calendar.DAY_OF_MONTH, day)

                                        // Time Picker
                                        val timePicker = TimePickerDialog(
                                            context,
                                            { _, hour, minute ->
                                                calendar.set(Calendar.HOUR_OF_DAY, hour)
                                                calendar.set(Calendar.MINUTE, minute)
                                                calendar.set(Calendar.SECOND, 0)
                                                selectedDateTime = calendar.timeInMillis
                                            },
                                            calendar.get(Calendar.HOUR_OF_DAY),
                                            calendar.get(Calendar.MINUTE),
                                            false
                                        )
                                        timePicker.show()
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                )
                                datePicker.show()
                            }
                            .testTag("planned_date_time_selector")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Alarm, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = sdf.format(Date(selectedDateTime)),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                            Icon(Icons.AutoMirrored.Default.KeyboardArrowRight, contentDescription = "Edit Date Time")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || destination.isBlank()) {
                        Toast.makeText(
                            context,
                            if (currentLang == AppLanguage.SINHALA) "කරුණාකර හේතුව සහ යන තැන ලියන්න!" else "Please fill out title and destination!",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    if (selectedDateTime < System.currentTimeMillis()) {
                        Toast.makeText(
                            context,
                            if (currentLang == AppLanguage.SINHALA) "කරුණාකර ඉදිරි දිනයක් සහ වෙලාවක් තෝරන්න!" else "Please select a future date and time!",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    onSave(title, destination, notes, selectedDateTime)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ),
                modifier = Modifier.testTag("save_planned_button")
            ) {
                Text(Translations.getString("save", currentLang))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Translations.getString("cancel", currentLang))
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

// ==========================================
// 3. ANALYTICS & INSIGHTS SCREEN
// ==========================================

@Composable
fun AnalyticsScreen(viewModel: TripViewModel, currentLang: AppLanguage) {
    val trips by viewModel.tripLogs.collectAsStateWithLifecycle()
    val planned by viewModel.plannedTrips.collectAsStateWithLifecycle()

    // 1. Calculations
    val totalDistance = remember(trips) { trips.sumOf { it.distanceKm } }
    val totalTrips = trips.size
    val activeAlarmsCount = remember(planned) {
        val now = System.currentTimeMillis()
        planned.count { it.scheduledTimeMillis > now }
    }

    val distanceA = remember(trips) {
        trips.filter { it.vehicleName == "Vehicle A" }.sumOf { it.distanceKm }
    }
    val distanceB = remember(trips) {
        trips.filter { it.vehicleName == "Vehicle B" }.sumOf { it.distanceKm }
    }

    val driversRank = remember(trips) {
        trips.filter { it.driverName.isNotBlank() }
            .groupBy { it.driverName }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
    }

    val assistantsRank = remember(trips) {
        trips.filter { it.assistantName.isNotBlank() }
            .groupBy { it.assistantName }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High level overview title
        item {
            Text(
                text = if (currentLang == AppLanguage.SINHALA) "ගමන් විස්තර සහ වාර්තා" else "Job Performance & Insights",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Summary Statistics row Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = Translations.getString("total_distance", currentLang),
                    value = String.format(Locale.US, "%.1f km", totalDistance),
                    icon = Icons.Default.TrendingUp,
                    tint = MaterialTheme.colorScheme.primary
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = Translations.getString("trips_logged", currentLang),
                    value = totalTrips.toString(),
                    icon = Icons.Default.Assignment,
                    tint = MaterialTheme.colorScheme.secondary
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = Translations.getString("active_reminders", currentLang),
                    value = activeAlarmsCount.toString(),
                    icon = Icons.Default.AlarmOn,
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        // Vehicle Usage comparison Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (currentLang == AppLanguage.SINHALA) "වාහන පාවිච්චි කරපු හැටි" else "Vehicle Usage Comparison",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Vehicle A Stat
                    VehicleStatProgress(
                        label = Translations.getString("stats_vehicle_a", currentLang),
                        distance = distanceA,
                        totalDistance = totalDistance,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Vehicle B Stat
                    VehicleStatProgress(
                        label = Translations.getString("stats_vehicle_b", currentLang),
                        distance = distanceB,
                        totalDistance = totalDistance,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // Frequent Crew Leaderboards Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (currentLang == AppLanguage.SINHALA) "නිතරම ගිය ඩ්‍රයිවර්ලා සහ හෙල්පර්ලා" else "Frequent Crew Leaders",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Drivers Leaderboard Column
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = Translations.getString("frequent_drivers", currentLang),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            if (driversRank.isEmpty()) {
                                Text("-", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                driversRank.forEachIndexed { idx, pair ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${idx + 1}. ${pair.first}",
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${pair.second}x",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Assistants Leaderboard Column
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = Translations.getString("frequent_assistants", currentLang),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            if (assistantsRank.isEmpty()) {
                                Text("-", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                assistantsRank.forEachIndexed { idx, pair ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${idx + 1}. ${pair.first}",
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${pair.second}x",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.border(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            RoundedCornerShape(12.dp)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(tint.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun VehicleStatProgress(
    label: String,
    distance: Double,
    totalDistance: Double,
    color: Color
) {
    val fraction = if (totalDistance > 0.0) (distance / totalDistance).toFloat() else 0f

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = String.format(Locale.US, "%.1f km (%.0f%%)", distance, fraction * 100),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { fraction },
            color = color,
            trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
        )
    }
}

// ==========================================
// COMMON UI COMPONENTS
// ==========================================

@Composable
fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.widthIn(max = 280.dp)
        )
    }
}

@Composable
fun SettingsScreen(viewModel: TripViewModel, currentLang: AppLanguage) {
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
    val drivers by viewModel.drivers.collectAsStateWithLifecycle()
    val assistants by viewModel.assistants.collectAsStateWithLifecycle()

    var activeSection by remember { mutableStateOf(0) } // 0: Vehicles, 1: Drivers, 2: Assistants, 3: Sync

    // Input States for Dialogs
    var showAddVehicleDialog by remember { mutableStateOf(false) }
    var showAddDriverDialog by remember { mutableStateOf(false) }
    var showAddAssistantDialog by remember { mutableStateOf(false) }

    var showPasswordDialogFor by remember { mutableStateOf<String?>(null) } // "vehicle", "driver", "assistant"
    var enteredPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Section Tabs
        TabRow(
            selectedTabIndex = activeSection,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeSection]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            Tab(
                selected = activeSection == 0,
                onClick = { activeSection = 0 },
                text = {
                    Text(
                        text = if (currentLang == AppLanguage.SINHALA) "වාහන" else "Vehicles",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                },
                icon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) }
            )
            Tab(
                selected = activeSection == 1,
                onClick = { activeSection = 1 },
                text = {
                    Text(
                        text = if (currentLang == AppLanguage.SINHALA) "ඩ්‍රයිවර්ලා" else "Drivers",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                },
                icon = { Icon(Icons.Default.Person, contentDescription = null) }
            )
            Tab(
                selected = activeSection == 2,
                onClick = { activeSection = 2 },
                text = {
                    Text(
                        text = if (currentLang == AppLanguage.SINHALA) "හෙල්පර්ලා" else "Assistants",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                },
                icon = { Icon(Icons.Default.Groups, contentDescription = null) }
            )
            Tab(
                selected = activeSection == 3,
                onClick = { activeSection = 3 },
                text = {
                    Text(
                        text = if (currentLang == AppLanguage.SINHALA) "Sync" else "Sync",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                },
                icon = { Icon(Icons.Default.Cloud, contentDescription = null) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (activeSection) {
                0 -> {
                    // Vehicles section
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = Translations.getString("vehicles_title", currentLang),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Button(
                                onClick = { showAddVehicleDialog = true },
                                modifier = Modifier.testTag("add_vehicle_button"),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = if (currentLang == AppLanguage.SINHALA) "එකතු කරන්න" else "Add", fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (vehicles.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (currentLang == AppLanguage.SINHALA) "තවම කිසිම වාහනයක් ඇතුළත් කරලා නැහැ." else "No vehicles configured.",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(vehicles) { vehicle ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(text = vehicle.plateNumber, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                                                    if (vehicle.description.isNotBlank()) {
                                                        Text(text = vehicle.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                }
                                            }
                                            IconButton(
                                                onClick = { viewModel.deleteVehicle(vehicle.id, vehicle.plateNumber) },
                                                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete Vehicle")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Drivers section
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = Translations.getString("drivers_title", currentLang),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Button(
                                onClick = { showAddDriverDialog = true },
                                modifier = Modifier.testTag("add_driver_button"),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = if (currentLang == AppLanguage.SINHALA) "එකතු කරන්න" else "Add", fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (drivers.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (currentLang == AppLanguage.SINHALA) "තවම කිසිම ඩ්‍රයිවර් කෙනෙක් ඇතුළත් කරලා නැහැ." else "No drivers configured.",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(drivers) { driver ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .background(
                                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                                            CircleShape
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        Icons.Default.Person,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(text = driver.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        // Standard uniform driver badge for all drivers
                                                        SuggestionChip(
                                                            onClick = {},
                                                            label = {
                                                                Text(
                                                                    text = if (currentLang == AppLanguage.SINHALA) "රියදුරු" else "Driver",
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            },
                                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                                                containerColor = MaterialTheme.colorScheme.primaryContainer
                                                            ),
                                                            border = null,
                                                            modifier = Modifier.height(20.dp)
                                                        )
                                                    }
                                                    if (driver.phone.isNotBlank()) {
                                                        Text(text = driver.phone, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                }
                                            }
                                            IconButton(
                                                onClick = { viewModel.deleteDriver(driver.id, driver.name) },
                                                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete Driver")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Assistants section
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = Translations.getString("assistants_title", currentLang),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Button(
                                onClick = { showAddAssistantDialog = true },
                                modifier = Modifier.testTag("add_assistant_button"),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = if (currentLang == AppLanguage.SINHALA) "එකතු කරන්න" else "Add", fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (assistants.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (currentLang == AppLanguage.SINHALA) "තවම කිසිම හෙල්පර් කෙනෙක් ඇතුළත් කරලා නැහැ." else "No assistants configured.",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(assistants) { assistant ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Default.Groups, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(text = assistant.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                                                    if (assistant.phone.isNotBlank()) {
                                                        Text(text = assistant.phone, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                }
                                            }
                                            IconButton(
                                                onClick = { viewModel.deleteAssistant(assistant.id, assistant.name) },
                                                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete Assistant")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // Cloud sync configurations
                    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
                    val lastSyncTime by viewModel.lastSyncTime.collectAsStateWithLifecycle()
                    val context = LocalContext.current

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = if (currentLang == AppLanguage.SINHALA) "ගූගල් ෂීට් ක්ලවුඩ් සමකාලීනය" else "Google Sheets Cloud Sync",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudQueue,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = if (currentLang == AppLanguage.SINHALA) "ක්ලවුඩ් තත්ත්වය: සක්‍රීයයි" else "Cloud Sync Status: Active",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (currentLang == AppLanguage.SINHALA) "ස්වයංක්‍රීයව තත්පර 45කට වරක් යාවත්කාලීන වේ" else "Auto-syncs in background every 45s",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                    val timeStr = if (lastSyncTime == 0L) {
                                        if (currentLang == AppLanguage.SINHALA) "තවම සිදුවී නැත" else "Never"
                                    } else {
                                        android.text.format.DateFormat.format("yyyy-MM-dd hh:mm a", lastSyncTime).toString()
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (currentLang == AppLanguage.SINHALA) "අවසන් සමමුහුර්තය:" else "Last Synced:",
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = timeStr,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.syncWithGoogleSheets { success, message ->
                                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        enabled = !isSyncing,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        if (isSyncing) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(Translations.getString("syncing", currentLang), fontSize = if (currentLang == AppLanguage.SINHALA) 11.sp else 13.sp)
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Sync,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(Translations.getString("sync_button", currentLang), fontSize = if (currentLang == AppLanguage.SINHALA) 11.sp else 13.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Password Prompt Dialog for Admin Settings Actions
    if (showPasswordDialogFor != null) {
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = {
                showPasswordDialogFor = null
                enteredPassword = ""
                passwordError = false
            },
            title = {
                Text(
                    text = Translations.getString("password_prompt", currentLang),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = Translations.getString("enter_password", currentLang),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = enteredPassword,
                        onValueChange = {
                            enteredPassword = it
                            passwordError = false
                        },
                        label = { Text(Translations.getString("password_label", currentLang)) },
                        modifier = Modifier.fillMaxWidth().testTag("password_input_field"),
                        singleLine = true,
                        isError = passwordError,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                    if (passwordError) {
                        Text(
                            text = Translations.getString("password_incorrect", currentLang),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (enteredPassword == "Ishan1993") {
                            val target = showPasswordDialogFor
                            showPasswordDialogFor = null
                            enteredPassword = ""
                            passwordError = false
                            
                            // Open corresponding dialog
                            when (target) {
                                "vehicle" -> showAddVehicleDialog = true
                                "driver" -> showAddDriverDialog = true
                                "assistant" -> showAddAssistantDialog = true
                            }
                        } else {
                            passwordError = true
                            Toast.makeText(
                                context,
                                Translations.getString("password_incorrect", currentLang),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.testTag("password_confirm_btn")
                ) {
                    Text(if (currentLang == AppLanguage.SINHALA) "තහවුරු කරන්න" else "Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPasswordDialogFor = null
                        enteredPassword = ""
                        passwordError = false
                    }
                ) {
                    Text(Translations.getString("cancel", currentLang))
                }
            }
        )
    }

    // New Vehicle Dialog
    if (showAddVehicleDialog) {
        var plateNumber by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        val context = LocalContext.current

        AlertDialog(
            onDismissRequest = { showAddVehicleDialog = false },
            title = { Text(Translations.getString("add_vehicle", currentLang), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = plateNumber,
                        onValueChange = { plateNumber = it },
                        label = { Text(Translations.getString("plate_number", currentLang)) },
                        modifier = Modifier.fillMaxWidth().testTag("vehicle_input_plate")
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(Translations.getString("description", currentLang)) },
                        modifier = Modifier.fillMaxWidth().testTag("vehicle_input_desc")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (plateNumber.isBlank()) {
                            Toast.makeText(context, if (currentLang == AppLanguage.SINHALA) "කරුණාකර වාහන අංකය ඇතුළත් කරන්න!" else "Please enter vehicle number!", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addVehicle(plateNumber.trim(), description.trim())
                            showAddVehicleDialog = false
                        }
                    },
                    modifier = Modifier.testTag("vehicle_save_btn")
                ) {
                    Text(Translations.getString("save", currentLang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddVehicleDialog = false }) {
                    Text(Translations.getString("cancel", currentLang))
                }
            }
        )
    }

    // New Driver Dialog
    if (showAddDriverDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        val context = LocalContext.current

        AlertDialog(
            onDismissRequest = { showAddDriverDialog = false },
            title = { Text(Translations.getString("add_driver", currentLang), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(Translations.getString("driver_name", currentLang)) },
                        modifier = Modifier.fillMaxWidth().testTag("driver_input_name")
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(Translations.getString("phone_number", currentLang)) },
                        modifier = Modifier.fillMaxWidth().testTag("driver_input_phone"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            Toast.makeText(context, if (currentLang == AppLanguage.SINHALA) "කරුණාකර රියදුරු නම ඇතුළත් කරන්න!" else "Please enter driver name!", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addDriver(name.trim(), phone.trim())
                            showAddDriverDialog = false
                        }
                    },
                    modifier = Modifier.testTag("driver_save_btn")
                ) {
                    Text(Translations.getString("save", currentLang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDriverDialog = false }) {
                    Text(Translations.getString("cancel", currentLang))
                }
            }
        )
    }

    // New Assistant Dialog
    if (showAddAssistantDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        val context = LocalContext.current

        AlertDialog(
            onDismissRequest = { showAddAssistantDialog = false },
            title = { Text(Translations.getString("add_assistant", currentLang), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(Translations.getString("assistant_name", currentLang)) },
                        modifier = Modifier.fillMaxWidth().testTag("assistant_input_name")
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(Translations.getString("phone_number", currentLang)) },
                        modifier = Modifier.fillMaxWidth().testTag("assistant_input_phone"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            Toast.makeText(context, if (currentLang == AppLanguage.SINHALA) "කරුණාකර හෙල්පර්ගේ නම ලියන්න!" else "Please enter assistant name!", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addAssistant(name.trim(), phone.trim())
                            showAddAssistantDialog = false
                        }
                    },
                    modifier = Modifier.testTag("assistant_save_btn")
                ) {
                    Text(Translations.getString("save", currentLang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAssistantDialog = false }) {
                    Text(Translations.getString("cancel", currentLang))
                }
            }
        )
    }
}

