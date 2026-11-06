package com.ongarand.gymtracker.logic

import android.content.Context

data class Settings(
    val weeklyTarget: Int = 4,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 18,
    val reminderMinute: Int = 0,
    val restSeconds: Int = 90
)

class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("ongarand_prefs", Context.MODE_PRIVATE)

    fun load(): Settings = Settings(
        weeklyTarget = sp.getInt("weeklyTarget", 4),
        reminderEnabled = sp.getBoolean("reminderEnabled", false),
        reminderHour = sp.getInt("reminderHour", 18),
        reminderMinute = sp.getInt("reminderMinute", 0),
        restSeconds = sp.getInt("restSeconds", 90)
    )

    fun save(s: Settings) {
        sp.edit()
            .putInt("weeklyTarget", s.weeklyTarget)
            .putBoolean("reminderEnabled", s.reminderEnabled)
            .putInt("reminderHour", s.reminderHour)
            .putInt("reminderMinute", s.reminderMinute)
            .putInt("restSeconds", s.restSeconds)
            .apply()
    }
}
