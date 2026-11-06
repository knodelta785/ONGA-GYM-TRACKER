package com.ongarand.gymtracker.logic

import com.ongarand.gymtracker.data.SetEntry
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

data class Suggestion(val text: String, val weight: Double, val reps: Int)

object Stats {
    /** Estimasi 1RM (rumus Epley). */
    fun e1rm(weight: Double, reps: Int): Double =
        if (reps <= 1) weight else weight * (1 + reps / 30.0)

    fun volume(sets: List<SetEntry>): Double = sets.sumOf { it.weight * it.reps }

    fun weekStart(d: LocalDate): LocalDate =
        d.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    /** Jumlah minggu berturut-turut yang mencapai target sesi. Minggu berjalan tidak memutus streak. */
    fun streakWeeks(sessionDays: List<LocalDate>, target: Int, today: LocalDate = LocalDate.now()): Int {
        val perWeek = sessionDays.distinct().groupBy { weekStart(it) }.mapValues { it.value.size }
        var week = weekStart(today)
        var streak = 0
        if ((perWeek[week] ?: 0) >= target) streak++
        week = week.minusWeeks(1)
        while ((perWeek[week] ?: 0) >= target) {
            streak++
            week = week.minusWeeks(1)
        }
        return streak
    }

    /** Saran progressive overload dari set sesi sebelumnya (urut waktu). */
    fun suggest(previousSets: List<SetEntry>): Suggestion? {
        if (previousSets.isEmpty()) return null
        val topWeight = previousSets.maxOf { it.weight }
        val bestReps = previousSets.filter { it.weight == topWeight }.maxOf { it.reps }
        return if (bestReps >= 10) {
            val next = topWeight + 2.5
            Suggestion("Saran: naikkan beban ke ${fmt(next)} kg x 8", next, 8)
        } else {
            Suggestion("Saran: ${fmt(topWeight)} kg x ${bestReps + 1} (tambah 1 repetisi)", topWeight, bestReps + 1)
        }
    }

    /** True jika 3 sesi terakhir tidak melampaui sesi sebelum itu. */
    fun isPlateau(bestPerSession: List<Double>): Boolean {
        if (bestPerSession.size < 4) return false
        val last3 = bestPerSession.takeLast(3)
        val before = bestPerSession[bestPerSession.size - 4]
        return last3.max() <= before
    }

    /** ID set yang memecahkan rekor 1RM sebelumnya (set pertama untuk suatu latihan bukan PR). */
    fun prSetIds(sets: List<SetEntry>): Set<Long> {
        val best = HashMap<Long, Double>()
        val result = HashSet<Long>()
        for (s in sets.sortedBy { it.timestamp }) {
            val v = e1rm(s.weight, s.reps)
            val prev = best[s.exerciseId]
            if (prev == null) {
                best[s.exerciseId] = v
            } else if (v > prev + 1e-9) {
                best[s.exerciseId] = v
                result.add(s.id)
            }
        }
        return result
    }

    fun movingAverage(values: List<Double>, window: Int): List<Double> =
        values.indices.map { i ->
            val from = maxOf(0, i - window + 1)
            values.subList(from, i + 1).average()
        }

    fun fmt(v: Double): String =
        if (v == Math.floor(v)) v.toLong().toString() else String.format("%.1f", v)
}
