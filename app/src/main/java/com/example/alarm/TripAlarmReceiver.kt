package com.example.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TripAlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "TripAlarmReceiver"
        private const val CHANNEL_ID = "vahana_trip_alarms"
        private const val CHANNEL_NAME = "Trip Reminders & Alarms"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val tripId = intent.getIntExtra("TRIP_ID", -1)
        val title = intent.getStringExtra("TRIP_TITLE") ?: "Scheduled Journey"
        val destination = intent.getStringExtra("TRIP_DESTINATION") ?: ""
        val reminderType = intent.getStringExtra("REMINDER_TYPE") ?: "ONE_HOUR"
        val scheduledTime = intent.getLongExtra("SCHEDULED_TIME", 0L)

        Log.d(TAG, "onReceive triggered: tripId=$tripId, reminderType=$reminderType, title=$title, destination=$destination")

        if (tripId == -1) return

        // Format scheduled time to show in notification
        val timeStr = if (scheduledTime > 0L) {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            sdf.format(Date(scheduledTime))
        } else {
            ""
        }

        // Generate title and description in English & Sinhala (multilingual support)
        val (notificationTitle, notificationBody) = when (reminderType) {
            "PREV_DAY" -> {
                Pair(
                    "Upcoming Journey Tomorrow / හෙට දින ගමනක් ඇත",
                    "Tomorrow: $title to $destination at $timeStr.\nහෙට: $title -> $destination, වේලාව: $timeStr"
                )
            }
            "FEW_HOURS" -> {
                Pair(
                    "Journey in a few hours / පැය කිහිපයකින් ගමන",
                    "Starts soon: $title to $destination at $timeStr.\nළඟදීම: $title -> $destination, වේලාව: $timeStr"
                )
            }
            else -> { // ONE_HOUR
                Pair(
                    "Journey in 1 HOUR! / තව පැයකින් ගමන!",
                    "Departure in 1 hour: $title to $destination ($timeStr)!\nපිටත් වීමට තව පැය 1යි: $title -> $destination ($timeStr)!"
                )
            }
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Set up custom notification sound (ALARM sound)
        val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Create the Notification Channel for Android Oreo (8.0) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent alerts for scheduled vehicle trips"
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
                setSound(alarmSoundUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // PendingIntent to launch MainActivity when clicked
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(context, tripId, launchIntent, flags)

        // Build notification
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // System alarm icon
            .setContentTitle(notificationTitle)
            .setContentText(notificationBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationBody))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(alarmSoundUri)
            .setVibrate(longArrayOf(0, 1000, 500, 1000, 500, 1000))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        // Notify
        // Notification ID can be unique per trip milestone to allow overlapping reminders
        val notificationId = tripId * 10 + when (reminderType) {
            "PREV_DAY" -> 0
            "FEW_HOURS" -> 1
            else -> 2
        }
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}
