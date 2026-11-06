package com.ongarand.gymtracker

import android.app.Application
import com.ongarand.gymtracker.reminder.createNotificationChannel

class OngarandApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(this)
    }
}
