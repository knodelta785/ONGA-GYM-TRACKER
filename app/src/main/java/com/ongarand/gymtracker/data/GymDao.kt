package com.ongarand.gymtracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GymDao {
    // Exercises
    @Query("SELECT * FROM exercises ORDER BY muscle, name")
    fun exercises(): Flow<List<Exercise>>

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun exerciseCount(): Int

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun exercise(id: Long): Exercise?

    @Insert
    suspend fun insertExercise(e: Exercise): Long

    @Insert
    suspend fun insertExercises(list: List<Exercise>)

    @Update
    suspend fun updateExercise(e: Exercise)

    @Query("DELETE FROM exercises WHERE id = :id")
    suspend fun deleteExercise(id: Long)

    // Sessions
    @Insert
    suspend fun insertSession(s: WorkoutSession): Long

    @Query("UPDATE sessions SET endTime = :end WHERE id = :id")
    suspend fun endSession(id: Long, end: Long)

    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    fun sessions(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM sessions WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    suspend fun openSession(): WorkoutSession?

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun deleteSession(id: Long)

    @Query("SELECT COUNT(*) FROM sessions WHERE startTime >= :from")
    suspend fun sessionCountSince(from: Long): Int

    // Sets
    @Insert
    suspend fun insertSet(s: SetEntry): Long

    @Query("DELETE FROM sets WHERE id = :id")
    suspend fun deleteSet(id: Long)

    @Query("DELETE FROM sets WHERE sessionId = :sessionId")
    suspend fun deleteSetsOfSession(sessionId: Long)

    @Query("DELETE FROM sets WHERE exerciseId = :exerciseId")
    suspend fun deleteSetsOfExercise(exerciseId: Long)

    @Query("SELECT COUNT(*) FROM sets WHERE sessionId = :sessionId")
    suspend fun setCount(sessionId: Long): Int

    @Query("SELECT * FROM sets ORDER BY timestamp")
    fun allSets(): Flow<List<SetEntry>>

    @Query("SELECT exerciseId FROM sets WHERE sessionId = :sessionId GROUP BY exerciseId ORDER BY MIN(timestamp)")
    suspend fun exerciseIdsOfSession(sessionId: Long): List<Long>

    @Query("SELECT * FROM sets WHERE exerciseId = :exerciseId AND sessionId != :sessionId ORDER BY timestamp DESC LIMIT 40")
    suspend fun recentSets(exerciseId: Long, sessionId: Long): List<SetEntry>

    // Body
    @Insert
    suspend fun insertBody(b: BodyEntry): Long

    @Query("DELETE FROM body_entries WHERE epochDay = :day")
    suspend fun deleteBodyDay(day: Long)

    @Query("DELETE FROM body_entries WHERE id = :id")
    suspend fun deleteBody(id: Long)

    @Query("SELECT * FROM body_entries ORDER BY epochDay")
    fun bodyEntries(): Flow<List<BodyEntry>>
}
