package com.ongarand.gymtracker.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ongarand.gymtracker.R
import com.ongarand.gymtracker.data.AppDatabase
import com.ongarand.gymtracker.data.SeedData
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.Duration
import java.util.concurrent.TimeUnit

const val CHANNEL_ID = "ongarand_reminder"
private const val WORK_NAME = "ongarand_daily_reminder"

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Pengingat latihan",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Pengingat harian untuk latihan gym" }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}

object ReminderScheduler {
    fun schedule(context: Context, hour: Int, minute: Int) {
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(hour, minute)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val delayMs = Duration.between(now, next).toMillis()

        val request = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val dao = AppDatabase.get(applicationContext).dao()
        val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (dao.sessionCountSince(startOfDay) > 0) return Result.success() // sudah latihan hari ini

        if (!NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
            return Result.success()
        }
        createNotificationChannel(applicationContext)
        val text = SeedData.motivationalReminders.random()
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Ongarand Gym Tracker")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(applicationContext).notify(1001, notification)
        } catch (_: SecurityException) {
            // izin notifikasi belum diberikan
        }
        return Result.success()
    }
}
