package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.PlannedTrip

object TripAlarmScheduler {
    private const val TAG = "TripAlarmScheduler"

    fun scheduleAlarmsForTrip(context: Context, trip: PlannedTrip) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val scheduledTime = trip.scheduledTimeMillis

        // Milestones to alarm:
        // 1. Previous Day (24 hours before)
        val prevDayTime = scheduledTime - (24 * 60 * 60 * 1000)
        // 2. Few Hours Before (3 hours before)
        val fewHoursTime = scheduledTime - (3 * 60 * 60 * 1000)
        // 3. One Hour Before (1 hour before)
        val oneHourTime = scheduledTime - (1 * 60 * 60 * 1000)

        val now = System.currentTimeMillis()

        // Schedule previous day alarm (RequestCode: trip.id * 3 + 0)
        if (prevDayTime > now) {
            scheduleAlarm(context, alarmManager, trip.id * 3 + 0, prevDayTime, trip, "PREV_DAY")
        } else {
            Log.d(TAG, "Prev day alarm is in the past for trip ${trip.id}")
        }

        // Schedule few hours alarm (RequestCode: trip.id * 3 + 1)
        if (fewHoursTime > now) {
            scheduleAlarm(context, alarmManager, trip.id * 3 + 1, fewHoursTime, trip, "FEW_HOURS")
        } else {
            Log.d(TAG, "Few hours alarm is in the past for trip ${trip.id}")
        }

        // Schedule one hour alarm (RequestCode: trip.id * 3 + 2)
        if (oneHourTime > now) {
            scheduleAlarm(context, alarmManager, trip.id * 3 + 2, oneHourTime, trip, "ONE_HOUR")
        } else {
            Log.d(TAG, "One hour alarm is in the past for trip ${trip.id}")
        }
    }

    fun cancelAlarmsForTrip(context: Context, tripId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        cancelAlarm(context, alarmManager, tripId * 3 + 0)
        cancelAlarm(context, alarmManager, tripId * 3 + 1)
        cancelAlarm(context, alarmManager, tripId * 3 + 2)
    }

    private fun scheduleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        requestCode: Int,
        triggerTimeMillis: Long,
        trip: PlannedTrip,
        reminderType: String
    ) {
        val intent = Intent(context, TripAlarmReceiver::class.java).apply {
            putExtra("TRIP_ID", trip.id)
            putExtra("TRIP_TITLE", trip.title)
            putExtra("TRIP_DESTINATION", trip.destination)
            putExtra("REMINDER_TYPE", reminderType)
            putExtra("SCHEDULED_TIME", trip.scheduledTimeMillis)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            flags
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Successfully scheduled alarm for trip ${trip.id}, type $reminderType at $triggerTimeMillis (RequestCode: $requestCode)")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException scheduling alarm: ${e.message}")
            // Fallback to inexact
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
            )
        }
    }

    private fun cancelAlarm(
        context: Context,
        alarmManager: AlarmManager,
        requestCode: Int
    ) {
        val intent = Intent(context, TripAlarmReceiver::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_NO_CREATE
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            flags
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled scheduled alarm with RequestCode: $requestCode")
        }
    }
}
