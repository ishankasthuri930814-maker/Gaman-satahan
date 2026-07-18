package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.TripLog
import com.example.ui.translation.AppLanguage
import com.example.ui.translation.Translations
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TripImageGenerator {

    class ThemeColors(
        val primary: Int,
        val secondary: Int,
        val backgroundGradientStart: Int,
        val backgroundGradientEnd: Int,
        val cardBackground: Int,
        val labelColor: Int,
        val valueColor: Int,
        val cardBorderColor: Int
    )

    fun getThemeColors(themeName: String): ThemeColors {
        return when (themeName) {
            "Blue" -> ThemeColors(
                primary = Color.parseColor("#1A2980"),
                secondary = Color.parseColor("#26D0CE"),
                backgroundGradientStart = Color.parseColor("#1F3A60"),
                backgroundGradientEnd = Color.parseColor("#0F2027"),
                cardBackground = Color.parseColor("#FFFFFF"),
                labelColor = Color.parseColor("#78909C"),
                valueColor = Color.parseColor("#263238"),
                cardBorderColor = Color.parseColor("#CFD8DC")
            )
            "Green" -> ThemeColors(
                primary = Color.parseColor("#004D40"),
                secondary = Color.parseColor("#00BFA5"),
                backgroundGradientStart = Color.parseColor("#0B5345"),
                backgroundGradientEnd = Color.parseColor("#041F1A"),
                cardBackground = Color.parseColor("#FFFFFF"),
                labelColor = Color.parseColor("#558B2F"),
                valueColor = Color.parseColor("#1B5E20"),
                cardBorderColor = Color.parseColor("#C8E6C9")
            )
            "Orange" -> ThemeColors(
                primary = Color.parseColor("#E65100"),
                secondary = Color.parseColor("#FFB300"),
                backgroundGradientStart = Color.parseColor("#78281F"),
                backgroundGradientEnd = Color.parseColor("#2C0E0B"),
                cardBackground = Color.parseColor("#FFFFFF"),
                labelColor = Color.parseColor("#E67E22"),
                valueColor = Color.parseColor("#5E35B1"),
                cardBorderColor = Color.parseColor("#FEDBB2")
            )
            "Red" -> ThemeColors(
                primary = Color.parseColor("#B71C1C"),
                secondary = Color.parseColor("#FF5252"),
                backgroundGradientStart = Color.parseColor("#641E16"),
                backgroundGradientEnd = Color.parseColor("#1F0606"),
                cardBackground = Color.parseColor("#FFFFFF"),
                labelColor = Color.parseColor("#E57373"),
                valueColor = Color.parseColor("#2C3E50"),
                cardBorderColor = Color.parseColor("#FFCDD2")
            )
            else -> ThemeColors( // Charcoal
                primary = Color.parseColor("#37474F"),
                secondary = Color.parseColor("#78909C"),
                backgroundGradientStart = Color.parseColor("#212F3D"),
                backgroundGradientEnd = Color.parseColor("#111822"),
                cardBackground = Color.parseColor("#1C2833"),
                labelColor = Color.parseColor("#AEB6BF"),
                valueColor = Color.parseColor("#F2F3F4"),
                cardBorderColor = Color.parseColor("#34495E")
            )
        }
    }

    fun generateTripCardUri(
        context: Context,
        trip: TripLog,
        currentLang: AppLanguage,
        themeName: String,
        showFields: Map<String, Boolean>
    ): Uri? {
        val width = 800
        val height = 950
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val colors = getThemeColors(themeName)

        // 1. Draw Gorgeous Background Gradient
        val bgPaint = Paint().apply { isAntiAlias = true }
        bgPaint.shader = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            colors.backgroundGradientStart, colors.backgroundGradientEnd,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 2. Decorative elements / Circles in background for visual interest
        val circlePaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            alpha = 15
            style = Paint.Style.FILL
        }
        canvas.drawCircle(width.toFloat(), 0f, 300f, circlePaint)
        canvas.drawCircle(0f, height.toFloat() * 0.8f, 200f, circlePaint)

        // 3. Draw Header Title on Canvas
        val titlePaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val subtitlePaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            alpha = 180
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        
        val headerText = if (currentLang == AppLanguage.SINHALA) "වාහන ගමන් විස්තරය" else "VAHANALOG TRIP MEMO"
        val subtitleText = if (currentLang == AppLanguage.SINHALA) "සුරක්ෂිත ගමනක් - Smart Fleet Assistant" else "Safe Journey - Smart Fleet Assistant"
        
        canvas.drawText(headerText, width / 2f, 75f, titlePaint)
        canvas.drawText(subtitleText, width / 2f, 110f, subtitlePaint)

        // 4. Draw Card Background Rounded Rect
        val cardMargin = 40f
        val cardRect = RectF(cardMargin, 150f, width - cardMargin, height - 120f)
        val cardPaint = Paint().apply {
            isAntiAlias = true
            color = colors.cardBackground
            style = Paint.Style.FILL
        }
        val cardBorderPaint = Paint().apply {
            isAntiAlias = true
            color = colors.cardBorderColor
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(cardRect, 24f, 24f, cardPaint)
        canvas.drawRoundRect(cardRect, 24f, 24f, cardBorderPaint)

        // 5. Draw Card Elements
        var currentY = 210f

        // Draw Destination in big font if selected
        if (showFields["destination"] == true) {
            val destLabelPaint = Paint().apply {
                isAntiAlias = true
                color = colors.labelColor
                textSize = 16f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val destTextPaint = Paint().apply {
                isAntiAlias = true
                color = colors.primary
                textSize = 36f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val destLabel = if (currentLang == AppLanguage.SINHALA) "ගමනාන්තය (DESTINATION)" else "DESTINATION"
            canvas.drawText("📍 $destLabel", cardMargin + 30f, currentY, destLabelPaint)
            currentY += 45f
            
            // Handle potentially long destination text
            val destText = trip.destination
            val maxTextWidth = width - 2 * cardMargin - 60f
            val measuredWidth = destTextPaint.measureText(destText)
            if (measuredWidth > maxTextWidth) {
                // Shrink text size if too long
                destTextPaint.textSize = 26f
            }
            canvas.drawText(destText, cardMargin + 30f, currentY, destTextPaint)
            currentY += 55f

            // Draw a subtle separator
            val sepPaint = Paint().apply {
                isAntiAlias = true
                color = colors.cardBorderColor
                strokeWidth = 2f
            }
            canvas.drawLine(cardMargin + 30f, currentY, width - cardMargin - 30f, currentY, sepPaint)
            currentY += 40f
        }

        // Draw Fields List
        val labelPaint = Paint().apply {
            isAntiAlias = true
            color = colors.labelColor
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val valPaint = Paint().apply {
            isAntiAlias = true
            color = colors.valueColor
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        fun drawFieldRow(emoji: String, label: String, value: String) {
            canvas.drawText("$emoji $label", cardMargin + 30f, currentY, labelPaint)
            currentY += 30f
            canvas.drawText(value, cardMargin + 60f, currentY, valPaint)
            currentY += 45f
        }

        // 1. Date & Time
        if (showFields["dateTime"] == true) {
            val sdf = SimpleDateFormat("yyyy MMM dd - hh:mm a", Locale.getDefault())
            val formattedDate = sdf.format(Date(trip.dateTimeMillis))
            val label = if (currentLang == AppLanguage.SINHALA) "දිනය සහ වේලාව" else "Date & Time"
            drawFieldRow("📅", label, formattedDate)
        }

        // 2. Driver
        if (showFields["driver"] == true) {
            val label = if (currentLang == AppLanguage.SINHALA) "රියදුරු" else "Driver"
            drawFieldRow("👤", label, trip.driverName.ifBlank { "-" })
        }

        // 3. Vehicle
        if (showFields["vehicle"] == true) {
            val label = if (currentLang == AppLanguage.SINHALA) "වාහනය" else "Vehicle"
            drawFieldRow("🚚", label, trip.vehicleName.ifBlank { "-" })
        }

        // 4. Assistant
        if (showFields["assistant"] == true) {
            val label = if (currentLang == AppLanguage.SINHALA) "හෙල්පර්" else "Assistant"
            drawFieldRow("🤝", label, trip.assistantName.ifBlank { "-" })
        }

        // 5. Distance
        if (showFields["distance"] == true) {
            val label = if (currentLang == AppLanguage.SINHALA) "දුර" else "Distance"
            drawFieldRow("📏", label, "${trip.distanceKm} km")
        }

        // 6. Reason
        if (showFields["reason"] == true && trip.reason.isNotBlank()) {
            val label = if (currentLang == AppLanguage.SINHALA) "හේතුව" else "Reason / Note"
            drawFieldRow("📝", label, trip.reason)
        }

        // 7. Fuel Details
        if (showFields["fuel"] == true && (trip.fuelOrderNumber.isNotBlank() || trip.fuelLiters > 0.0)) {
            val label = if (currentLang == AppLanguage.SINHALA) "ඉන්ධන විස්තර" else "Fuel Details"
            val valStr = buildString {
                if (trip.fuelLiters > 0.0) append("${trip.fuelLiters} L ")
                if (trip.fuelOrderNumber.isNotBlank()) append("(${trip.fuelOrderNumber})")
            }
            drawFieldRow("⛽", label, valStr)
        }

        // 6. Draw Footer Branding on the visual card
        val brandPaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            alpha = 160
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        val genDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val footerLabel = if (currentLang == AppLanguage.SINHALA) {
            "VahanaLog මඟින් ජනනය කරන ලදි | $genDate"
        } else {
            "Generated via VahanaLog Smart System | $genDate"
        }
        canvas.drawText(footerLabel, width / 2f, height - 60f, brandPaint)

        // 7. Save Bitmap to file cache for secure sharing
        return try {
            val sharedDir = File(context.cacheDir, "shared_images")
            if (!sharedDir.exists()) {
                sharedDir.mkdirs()
            }
            val imageFile = File(sharedDir, "trip_report_${trip.id}.png")
            val out = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()

            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
