package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OdometerCalculation
import com.example.ui.translation.AppLanguage
import com.example.ui.viewmodel.TripViewModel
import java.text.SimpleDateFormat
import java.util.*

private val calcTranslations = mapOf(
    "screen_title" to mapOf(AppLanguage.ENGLISH to "Odometer & Fuel Calculator", AppLanguage.SINHALA to "ධාවන මීටර සහ ඉන්ධන ගණකය"),
    "vehicle_label" to mapOf(AppLanguage.ENGLISH to "Vehicle Name / Plate No.", AppLanguage.SINHALA to "වාහනයේ නම / අංකය"),
    "start_odo" to mapOf(AppLanguage.ENGLISH to "Start Odometer (km)", AppLanguage.SINHALA to "ආරම්භක මීටරය (km)"),
    "efficiency" to mapOf(AppLanguage.ENGLISH to "Fuel Efficiency (km per 1L)", AppLanguage.SINHALA to "ලීටරයකින් දුවන දුර (km/L)"),
    "consumed" to mapOf(AppLanguage.ENGLISH to "Consumed Fuel (Liters)", AppLanguage.SINHALA to "භාවිතා කළ ඉන්ධන (ලීටර)"),
    "initial_fuel" to mapOf(AppLanguage.ENGLISH to "Initial Fuel in Tank (Liters)", AppLanguage.SINHALA to "ආරම්භයේදී ටැංකියේ තිබූ ඉන්ධන (ලීටර)"),
    "obtained_fuel" to mapOf(AppLanguage.ENGLISH to "Fuel Obtained/Pumped (Liters)", AppLanguage.SINHALA to "අලුතින් ලබාගත් ඉන්ධන (ලීටර)"),
    "calc_results" to mapOf(AppLanguage.ENGLISH to "Calculation Results", AppLanguage.SINHALA to "ගණනය කළ ප්‍රතිඵල"),
    "distance_traveled" to mapOf(AppLanguage.ENGLISH to "Distance Traveled", AppLanguage.SINHALA to "ධාවනය කළ දුර"),
    "final_odo" to mapOf(AppLanguage.ENGLISH to "End Odometer Reading", AppLanguage.SINHALA to "අවසාන මීටර කියවීම"),
    "remaining_fuel" to mapOf(AppLanguage.ENGLISH to "Remaining Fuel in Tank", AppLanguage.SINHALA to "ටැංකියේ ඉතිරි ඉන්ධන"),
    "btn_save" to mapOf(AppLanguage.ENGLISH to "Save to Logbook", AppLanguage.SINHALA to "සටහන සුරකින්න"),
    "btn_clear" to mapOf(AppLanguage.ENGLISH to "Clear Inputs", AppLanguage.SINHALA to "හිස් කරන්න"),
    "history_title" to mapOf(AppLanguage.ENGLISH to "Saved Calculations", AppLanguage.SINHALA to "සුරැකි ගණනය කිරීම්"),
    "empty_history" to mapOf(AppLanguage.ENGLISH to "No saved calculations yet", AppLanguage.SINHALA to "තවමත් සුරැකි ගණනය කිරීම් නොමැත"),
    "success_save" to mapOf(AppLanguage.ENGLISH to "Calculation saved successfully!", AppLanguage.SINHALA to "ගණනය කිරීම සාර්ථකව සුරකින ලදි!"),
    "error_empty" to mapOf(AppLanguage.ENGLISH to "Please fill in all required fields", AppLanguage.SINHALA to "කරුණාකර සියලුම අත්‍යවශ්‍ය තොරතුරු ඇතුළත් කරන්න"),
    "confirm_clear" to mapOf(AppLanguage.ENGLISH to "Are you sure you want to clear all saved calculations?", AppLanguage.SINHALA to "සියලුම සුරැකි ගණනය කිරීම් මැකීමට අවශ්‍ය බව ස්ථිරද?"),
    "delete_title" to mapOf(AppLanguage.ENGLISH to "Delete Saved Log", AppLanguage.SINHALA to "සුරැකි සටහන මකන්න"),
    "cancel" to mapOf(AppLanguage.ENGLISH to "Cancel", AppLanguage.SINHALA to "අවලංගු කරන්න"),
    "clear_all" to mapOf(AppLanguage.ENGLISH to "Clear All", AppLanguage.SINHALA to "සියල්ල මකන්න"),
    "vehicle_select_hint" to mapOf(AppLanguage.ENGLISH to "Tap to select from settings", AppLanguage.SINHALA to "Settings වලින් තෝරා ගැනීමට තට්ටු කරන්න")
)

private fun getStr(key: String, lang: AppLanguage): String {
    return calcTranslations[key]?.get(lang) ?: key
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: TripViewModel,
    currentLang: AppLanguage
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    
    // Inputs States
    var vehicleName by remember { mutableStateOf("") }
    var startOdoText by remember { mutableStateOf("") }
    var efficiencyText by remember { mutableStateOf("") }
    var consumedText by remember { mutableStateOf("") }
    var initialFuelText by remember { mutableStateOf("") }
    var obtainedFuelText by remember { mutableStateOf("") }

    // Dropdown list
    val vehicles by viewModel.vehicles.collectAsState()
    var showVehicleDropdown by remember { mutableStateOf(false) }

    // Observed Saved Calculations
    val savedCalculations by viewModel.odometerCalculations.collectAsState()

    // Parsing values safely for real-time calculation preview
    val startOdo = startOdoText.toDoubleOrNull() ?: 0.0
    val efficiency = efficiencyText.toDoubleOrNull() ?: 0.0
    val consumed = consumedText.toDoubleOrNull() ?: 0.0
    val initialFuel = initialFuelText.toDoubleOrNull() ?: 0.0
    val obtained = obtainedFuelText.toDoubleOrNull() ?: 0.0

    // Math formulas
    val calculatedDistance = consumed * efficiency
    val finalOdometer = startOdo + calculatedDistance
    val remainingFuel = initialFuel + obtained - consumed

    var showClearConfirm by remember { mutableStateOf(false) }

    // Confirmation dialog for wipe-out
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(getStr("clear_all", currentLang)) },
            text = { Text(getStr("confirm_clear", currentLang)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllOdometerCalculations()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(getStr("clear_all", currentLang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text(getStr("cancel", currentLang))
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // 1. Header Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = getStr("screen_title", currentLang),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (currentLang == AppLanguage.SINHALA) 
                            "වාහන වල සහන් පොතේ සටහන් කිරීම සඳහා ගණන් හදන්න" 
                            else "Calculate and log parameters for vehicle log books",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // 2. Input Fields Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Vehicle Name/Plate Input with custom dropdown picker
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = vehicleName,
                            onValueChange = { vehicleName = it },
                            label = { Text(getStr("vehicle_label", currentLang)) },
                            leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                            trailingIcon = {
                                if (vehicles.isNotEmpty()) {
                                    IconButton(onClick = { showVehicleDropdown = !showVehicleDropdown }) {
                                        Icon(
                                            imageVector = if (showVehicleDropdown) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                            contentDescription = "Show Vehicles List"
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("calc_vehicle_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Custom Dropdown Menu for configured vehicles
                        DropdownMenu(
                            expanded = showVehicleDropdown,
                            onDismissRequest = { showVehicleDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            vehicles.forEach { vehicle ->
                                DropdownMenuItem(
                                    text = { Text(text = "${vehicle.plateNumber} (${vehicle.description})") },
                                    onClick = {
                                        vehicleName = vehicle.plateNumber
                                        showVehicleDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Start Odometer & Fuel Efficiency in a Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = startOdoText,
                            onValueChange = { startOdoText = it },
                            label = { Text(getStr("start_odo", currentLang)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("calc_start_odo"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = efficiencyText,
                            onValueChange = { efficiencyText = it },
                            label = { Text(getStr("efficiency", currentLang)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("calc_efficiency"),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Consumed Fuel
                    OutlinedTextField(
                        value = consumedText,
                        onValueChange = { consumedText = it },
                        label = { Text(getStr("consumed", currentLang)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = { Icon(Icons.Default.LocalGasStation, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calc_consumed_fuel"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Initial Fuel in Tank & Obtained Fuel in a Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = initialFuelText,
                            onValueChange = { initialFuelText = it },
                            label = { Text(getStr("initial_fuel", currentLang)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("calc_initial_fuel"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = obtainedFuelText,
                            onValueChange = { obtainedFuelText = it },
                            label = { Text(getStr("obtained_fuel", currentLang)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("calc_obtained_fuel"),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Clear Inputs Button
                        OutlinedButton(
                            onClick = {
                                vehicleName = ""
                                startOdoText = ""
                                efficiencyText = ""
                                consumedText = ""
                                initialFuelText = ""
                                obtainedFuelText = ""
                                focusManager.clearFocus()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(getStr("btn_clear", currentLang))
                        }

                        // Save Button
                        Button(
                            onClick = {
                                if (vehicleName.isBlank() || startOdoText.isBlank() || efficiencyText.isBlank() || consumedText.isBlank()) {
                                    Toast.makeText(context, getStr("error_empty", currentLang), Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.insertOdometerCalculation(
                                        vehicleName = vehicleName,
                                        initialOdometer = startOdo,
                                        fuelEfficiency = efficiency,
                                        fuelConsumed = consumed,
                                        initialFuel = initialFuel,
                                        fuelObtained = obtained,
                                        finalOdometer = finalOdometer,
                                        remainingFuel = remainingFuel
                                    )
                                    Toast.makeText(context, getStr("success_save", currentLang), Toast.LENGTH_SHORT).show()
                                    // Clear fields after saving to prevent duplicate saving
                                    startOdoText = String.format(Locale.US, "%.1f", finalOdometer) // Suggest end odometer as next starting odometer!
                                    consumedText = ""
                                    obtainedFuelText = ""
                                    initialFuelText = String.format(Locale.US, "%.1f", remainingFuel) // Suggest remaining fuel as next initial fuel!
                                    focusManager.clearFocus()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("calc_save_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(getStr("btn_save", currentLang))
                        }
                    }
                }
            }
        }

        // 3. Calculation Preview Panel (Real-time update)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = getStr("calc_results", currentLang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))

                    // Traveled distance display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(getStr("distance_traveled", currentLang), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Text(
                            text = "${String.format(Locale.US, "%.1f", calculatedDistance)} km",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // End Odometer reading display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(getStr("final_odo", currentLang), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Text(
                            text = "${String.format(Locale.US, "%.1f", finalOdometer)} km",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    // Remaining Fuel display
                    val remainingFuelColor = if (remainingFuel < 5.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = remainingFuelColor, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(getStr("remaining_fuel", currentLang), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Text(
                            text = "${String.format(Locale.US, "%.1f", remainingFuel)} L",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = remainingFuelColor
                        )
                    }
                }
            }
        }

        // 4. Saved Log History Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${getStr("history_title", currentLang)} (${savedCalculations.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (savedCalculations.isNotEmpty()) {
                    TextButton(
                        onClick = { showClearConfirm = true },
                        colors = ButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.error,
                            disabledContainerColor = Color.Transparent,
                            disabledContentColor = Color.LightGray
                        )
                    ) {
                        Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(getStr("clear_all", currentLang), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 5. Empty State or Saved Cards list
        if (savedCalculations.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = getStr("empty_history", currentLang),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(savedCalculations, key = { it.id }) { item ->
                SavedCalculationCard(
                    item = item,
                    currentLang = currentLang,
                    onDelete = { viewModel.deleteOdometerCalculation(item.id) }
                )
            }
        }
    }
}

@Composable
fun SavedCalculationCard(
    item: OdometerCalculation,
    currentLang: AppLanguage,
    onDelete: () -> Unit
) {
    val sdf = SimpleDateFormat("yyyy MMM dd - hh:mm a", Locale.getDefault())
    val formattedDate = sdf.format(Date(item.dateTimeMillis))

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Vehicle Name & Time & Delete icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = item.vehicleName.ifBlank { "No vehicle" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = formattedDate,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete Calculation Record",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            // Main Data Grid (Start Odo, Traveled Distance, End Odo, Remaining Fuel)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Left column: Odometer Info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (currentLang == AppLanguage.SINHALA) "ධාවන මීටර කියවීම්" else "Odometer readings",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• ${if (currentLang == AppLanguage.SINHALA) "ආරම්භක" else "Start"}: ${String.format(Locale.US, "%.1f", item.initialOdometer)} km",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "• ${if (currentLang == AppLanguage.SINHALA) "අවසාන" else "End"}: ${String.format(Locale.US, "%.1f", item.finalOdometer)} km",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "• ${if (currentLang == AppLanguage.SINHALA) "දුවපු දුර" else "Distance"}: ${String.format(Locale.US, "%.1f", (item.fuelConsumed * item.fuelEfficiency))} km",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Right column: Fuel Info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (currentLang == AppLanguage.SINHALA) "ඉන්ධන විස්තර" else "Fuel details",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• ${if (currentLang == AppLanguage.SINHALA) "භාවිතය" else "Consumed"}: ${item.fuelConsumed} L (@ ${item.fuelEfficiency} km/L)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (item.fuelObtained > 0.0) {
                        Text(
                            text = "• ${if (currentLang == AppLanguage.SINHALA) "ලබාගත්" else "Added"}: +${item.fuelObtained} L",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "• ${if (currentLang == AppLanguage.SINHALA) "ඉතිරි" else "Remaining"}: ${String.format(Locale.US, "%.1f", item.remainingFuel)} L",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.remainingFuel < 5.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}
