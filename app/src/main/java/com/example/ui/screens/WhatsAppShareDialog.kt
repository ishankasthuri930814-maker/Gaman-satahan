package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TripLog
import com.example.ui.translation.AppLanguage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppShareDialog(
    trip: TripLog,
    currentLang: AppLanguage,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // 1. Sharing Mode: Text or Image
    var shareMode by remember { mutableStateOf("image") } // "text" or "image"

    // 2. Theme Selection
    var selectedTheme by remember { mutableStateOf("Blue") } // "Blue", "Green", "Orange", "Red", "Charcoal"

    // 3. Selection of fields to share
    var showDestination by remember { mutableStateOf(true) }
    var showDateTime by remember { mutableStateOf(true) }
    var showDriver by remember { mutableStateOf(true) }
    var showVehicle by remember { mutableStateOf(true) }
    var showAssistant by remember { mutableStateOf(true) }
    var showDistance by remember { mutableStateOf(true) }
    var showReason by remember { mutableStateOf(true) }
    var showFuel by remember { mutableStateOf(true) }

    // 4. Custom note/footer to append
    var customNote by remember { mutableStateOf("") }

    val showFields = remember(
        showDestination, showDateTime, showDriver, showVehicle,
        showAssistant, showDistance, showReason, showFuel
    ) {
        mapOf(
            "destination" to showDestination,
            "dateTime" to showDateTime,
            "driver" to showDriver,
            "vehicle" to showVehicle,
            "assistant" to showAssistant,
            "distance" to showDistance,
            "reason" to showReason,
            "fuel" to showFuel
        )
    }

    // Predefined colors for preview circles
    val themeOptions = listOf(
        Triple("Blue", Color(0xFF1A2980), Color(0xFF26D0CE)),
        Triple("Green", Color(0xFF004D40), Color(0xFF00BFA5)),
        Triple("Orange", Color(0xFFE65100), Color(0xFFFFB300)),
        Triple("Red", Color(0xFFB71C1C), Color(0xFFFF5252)),
        Triple("Charcoal", Color(0xFF37474F), Color(0xFF78909C))
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (currentLang == AppLanguage.SINHALA) "WhatsApp වෙත යොමු කරන්න" else "Share to WhatsApp",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Formatting Mode Selection
                Text(
                    text = if (currentLang == AppLanguage.SINHALA) "බෙදාගැනීමේ ක්‍රමය තෝරන්න:" else "Select Sharing Format:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Image Card Mode
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { shareMode = "image" }
                            .border(
                                width = 2.dp,
                                color = if (shareMode == "image") MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (shareMode == "image") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = if (shareMode == "image") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (currentLang == AppLanguage.SINHALA) "වර්ණවත් කාඩ්පත" else "Visual Card",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (shareMode == "image") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Text mode
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { shareMode = "text" }
                            .border(
                                width = 2.dp,
                                color = if (shareMode == "text") MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (shareMode == "text") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = if (shareMode == "text") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (currentLang == AppLanguage.SINHALA) "පැහැදිලි පෙළ" else "Rich Text",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (shareMode == "text") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Theme selection (Only for Visual Card or custom emoji themes)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (currentLang == AppLanguage.SINHALA) "තේමාවේ වර්ණය තෝරන්න:" else "Select Theme Color:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        themeOptions.forEach { (name, startColor, endColor) ->
                            val isSelected = selectedTheme == name
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(startColor)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f),
                                        shape = CircleShape
                                    )
                                    .clickable { selectedTheme = name }
                            )
                        }
                    }
                }

                // Field Selector list (Checkboxes)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (currentLang == AppLanguage.SINHALA) "ඇතුළත් කළ යුතු විස්තර තෝරන්න:" else "Select Fields to Include:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Destination Checkbox
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { showDestination = !showDestination }) {
                        Checkbox(checked = showDestination, onCheckedChange = { showDestination = it })
                        Text(text = if (currentLang == AppLanguage.SINHALA) "📍 ගමනාන්තය (Destination)" else "📍 Destination", fontSize = 14.sp)
                    }

                    // DateTime Checkbox
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { showDateTime = !showDateTime }) {
                        Checkbox(checked = showDateTime, onCheckedChange = { showDateTime = it })
                        Text(text = if (currentLang == AppLanguage.SINHALA) "📅 දිනය සහ වේලාව (Date & Time)" else "📅 Date & Time", fontSize = 14.sp)
                    }

                    // Driver Checkbox
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { showDriver = !showDriver }) {
                        Checkbox(checked = showDriver, onCheckedChange = { showDriver = it })
                        Text(text = if (currentLang == AppLanguage.SINHALA) "👤 රියදුරු (Driver)" else "👤 Driver", fontSize = 14.sp)
                    }

                    // Vehicle Checkbox
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { showVehicle = !showVehicle }) {
                        Checkbox(checked = showVehicle, onCheckedChange = { showVehicle = it })
                        Text(text = if (currentLang == AppLanguage.SINHALA) "🚚 වාහනය (Vehicle)" else "🚚 Vehicle", fontSize = 14.sp)
                    }

                    // Assistant Checkbox
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { showAssistant = !showAssistant }) {
                        Checkbox(checked = showAssistant, onCheckedChange = { showAssistant = it })
                        Text(text = if (currentLang == AppLanguage.SINHALA) "🤝 හෙල්පර් (Assistant)" else "🤝 Assistant", fontSize = 14.sp)
                    }

                    // Distance Checkbox
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { showDistance = !showDistance }) {
                        Checkbox(checked = showDistance, onCheckedChange = { showDistance = it })
                        Text(text = if (currentLang == AppLanguage.SINHALA) "📏 දුර ප්‍රමාණය (Distance)" else "📏 Distance", fontSize = 14.sp)
                    }

                    // Reason Checkbox
                    if (trip.reason.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { showReason = !showReason }) {
                            Checkbox(checked = showReason, onCheckedChange = { showReason = it })
                            Text(text = if (currentLang == AppLanguage.SINHALA) "📝 ගමනේ හේතුව (Reason)" else "📝 Reason / Note", fontSize = 14.sp)
                        }
                    }

                    // Fuel Checkbox
                    if (trip.fuelLiters > 0.0 || trip.fuelOrderNumber.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { showFuel = !showFuel }) {
                            Checkbox(checked = showFuel, onCheckedChange = { showFuel = it })
                            Text(text = if (currentLang == AppLanguage.SINHALA) "⛽ ඉන්ධන විස්තර (Fuel Details)" else "⛽ Fuel Details", fontSize = 14.sp)
                        }
                    }
                }

                // Custom notes input
                OutlinedTextField(
                    value = customNote,
                    onValueChange = { customNote = it },
                    label = { Text(if (currentLang == AppLanguage.SINHALA) "අමතර සටහන (Custom Message)" else "Add Custom Note") },
                    placeholder = { Text(if (currentLang == AppLanguage.SINHALA) "මෙහි ලියන්න..." else "Type something to append...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalMessage = buildWhatsAppMessage(trip, currentLang, showFields, customNote, selectedTheme)
                    
                    if (shareMode == "image") {
                        // Generate beautifully colored visual card image first
                        val imageUri = TripImageGenerator.generateTripCardUri(
                            context = context,
                            trip = trip,
                            currentLang = currentLang,
                            themeName = selectedTheme,
                            showFields = showFields
                        )
                        if (imageUri != null) {
                            shareWhatsApp(context, finalMessage, imageUri)
                        } else {
                            Toast.makeText(context, "Error generating card image!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Share rich text format
                        shareWhatsApp(context, finalMessage, null)
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)) // WhatsApp Green
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (currentLang == AppLanguage.SINHALA) "යවන්න (WhatsApp Share)" else "Send to WhatsApp",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = if (currentLang == AppLanguage.SINHALA) "අවලංගු කරන්න" else "Cancel")
            }
        }
    )
}

private fun buildWhatsAppMessage(
    trip: TripLog,
    currentLang: AppLanguage,
    showFields: Map<String, Boolean>,
    customNote: String,
    themeColor: String
): String {
    // Pick colorful bullet emojis matching selected color theme
    val emojiBullet = when (themeColor) {
        "Blue" -> "🔵"
        "Green" -> "🟢"
        "Orange" -> "🟠"
        "Red" -> "🔴"
        else -> "⚫"
    }

    val headerEmoji = when (themeColor) {
        "Blue" -> "🚙💨"
        "Green" -> "🚚🌟"
        "Orange" -> "🛣️🔥"
        "Red" -> "🚨⚡"
        else -> "🚛🕶️"
    }

    return buildString {
        // Title block
        val titleText = if (currentLang == AppLanguage.SINHALA) "=== වාහන ගමන් විස්තරය ===" else "=== VAHANALONG TRIP MEMO ==="
        append("*$titleText* $headerEmoji\n\n")

        // 1. Destination
        if (showFields["destination"] == true) {
            val label = if (currentLang == AppLanguage.SINHALA) "ගමනාන්තය" else "Destination"
            append("$emojiBullet *$label:* ${trip.destination}\n")
        }

        // 2. Date & Time
        if (showFields["dateTime"] == true) {
            val sdf = SimpleDateFormat("yyyy MMM dd - hh:mm a", Locale.getDefault())
            val formattedDate = sdf.format(Date(trip.dateTimeMillis))
            val label = if (currentLang == AppLanguage.SINHALA) "දිනය සහ වේලාව" else "Date & Time"
            append("$emojiBullet *$label:* $formattedDate\n")
        }

        // 3. Driver
        if (showFields["driver"] == true) {
            val label = if (currentLang == AppLanguage.SINHALA) "රියදුරු" else "Driver"
            append("$emojiBullet *$label:* ${trip.driverName.ifBlank { "-" }}\n")
        }

        // 4. Vehicle
        if (showFields["vehicle"] == true) {
            val label = if (currentLang == AppLanguage.SINHALA) "වාහනය" else "Vehicle"
            append("$emojiBullet *$label:* ${trip.vehicleName.ifBlank { "-" }}\n")
        }

        // 5. Assistant
        if (showFields["assistant"] == true) {
            val label = if (currentLang == AppLanguage.SINHALA) "හෙල්පර්" else "Assistant"
            append("$emojiBullet *$label:* ${trip.assistantName.ifBlank { "-" }}\n")
        }

        // 6. Distance
        if (showFields["distance"] == true) {
            val label = if (currentLang == AppLanguage.SINHALA) "දුර ප්‍රමාණය" else "Distance"
            append("$emojiBullet *$label:* ${trip.distanceKm} km\n")
        }

        // 7. Reason
        if (showFields["reason"] == true && trip.reason.isNotBlank()) {
            val label = if (currentLang == AppLanguage.SINHALA) "ගමනේ හේතුව" else "Reason / Note"
            append("$emojiBullet *$label:* ${trip.reason}\n")
        }

        // 8. Fuel details
        if (showFields["fuel"] == true && (trip.fuelLiters > 0.0 || trip.fuelOrderNumber.isNotBlank())) {
            val label = if (currentLang == AppLanguage.SINHALA) "ඉන්ධන විස්තර" else "Fuel"
            val valStr = buildString {
                if (trip.fuelLiters > 0.0) append("${trip.fuelLiters} L ")
                if (trip.fuelOrderNumber.isNotBlank()) append("(${trip.fuelOrderNumber})")
            }
            append("$emojiBullet *$label:* $valStr\n")
        }

        // Custom note
        if (customNote.isNotBlank()) {
            append("\n💬 *Note:* _${customNote.trim()}_\n")
        }

        append("\n-----------------------------\n")
        append("🛣️ *Safe travels with VahanaLog* 🛣️")
    }
}

private fun shareWhatsApp(context: Context, text: String, imageUri: Uri? = null) {
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        if (imageUri != null) {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
    }

    // Try sending directly to WhatsApp standard
    val whatsappIntent = Intent(intent).apply {
        `package` = "com.whatsapp"
    }

    try {
        context.startActivity(whatsappIntent)
    } catch (e: Exception) {
        // Fall back to WhatsApp Business
        val whatsappBusinessIntent = Intent(intent).apply {
            `package` = "com.whatsapp.w4b"
        }
        try {
            context.startActivity(whatsappBusinessIntent)
        } catch (e2: Exception) {
            // General share sheet if WhatsApp is completely missing
            val chooser = Intent.createChooser(intent, "Share via")
            context.startActivity(chooser)
        }
    }
}
