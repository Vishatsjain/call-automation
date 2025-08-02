package com.vibecoder.app.workers

import android.content.Context
import androidx.work.*
import com.vibecoder.app.models.NotificationTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.datetime.*
import java.util.*

@Singleton
class NotificationScheduler @Inject constructor(
    private val context: Context
) {
    
    fun scheduleNotifications(notificationTimes: List<NotificationTime>) {
        // Cancel all existing work
        WorkManager.getInstance(context).cancelAllWorkByTag("call_reminders")
        
        notificationTimes.filter { it.isEnabled }.forEach { notificationTime ->
            scheduleNotification(notificationTime)
        }
    }
    
    private fun scheduleNotification(notificationTime: NotificationTime) {
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(calculateInitialDelay(notificationTime), TimeUnit.MILLISECONDS)
            .addTag("call_reminders")
            .addTag("notification_${notificationTime.id}")
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .build()
        
        WorkManager.getInstance(context).enqueue(workRequest)
    }
    
    private fun calculateInitialDelay(notificationTime: NotificationTime): Long {
        val now = Clock.System.now()
        val currentZone = TimeZone.currentSystemDefault()
        val currentDateTime = now.toLocalDateTime(currentZone)
        
        // Create target time for today
        val targetDate = currentDateTime.date
        val targetTime = LocalTime(notificationTime.hour, notificationTime.minute)
        val targetDateTime = targetDate.atTime(targetTime)
        
        // If target time has passed today, schedule for tomorrow
        val finalTargetDateTime = if (targetDateTime <= currentDateTime) {
            targetDate.plus(1, DateTimeUnit.DAY).atTime(targetTime)
        } else {
            targetDateTime
        }
        
        val targetInstant = finalTargetDateTime.toInstant(currentZone)
        return (targetInstant - now).inWholeMilliseconds
    }
    
    fun cancelNotification(notificationTimeId: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag("notification_$notificationTimeId")
    }
    
    fun cancelAllNotifications() {
        WorkManager.getInstance(context).cancelAllWorkByTag("call_reminders")
    }
}