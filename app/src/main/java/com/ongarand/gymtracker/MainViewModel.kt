package com.ongarand.gymtracker

import android.app.Application
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ongarand.gymtracker.data.AppDatabase
import com.ongarand.gymtracker.data.BodyEntry
import com.ongarand.gymtracker.data.Exercise
import com.ongarand.gymtracker.data.SeedData
import com.ongarand.gymtracker.data.SetEntry
import com.ongarand.gymtracker.data.WorkoutSession
import com.ongarand.gymtracker.logic.Prefs
import com.ongarand.gymtracker.logic.Settings
import com.ongarand.gymtracker.reminder.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).dao()
    private val prefs = Prefs(app)

    val exercises: StateFlow<List<Exercise>> =
        dao.exercises().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val sessions: StateFlow<List<WorkoutSession>> =
        dao.sessions().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val allSets: StateFlow<List<SetEntry>> =
        dao.allSets().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val bodyEntries: StateFlow<List<BodyEntry>> =
        dao.bodyEntries().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _activeSessionId = MutableStateFlow<Long?>(null)
    val activeSessionId: StateFlow<Long?> = _activeSessionId

    private val _activeStart = MutableStateFlow(0L)
    val activeStart: StateFlow<Long> = _activeStart

    private val _sessionExercises = MutableStateFlow<List<Long>>(emptyList())
    val sessionExercises: StateFlow<List<Long>> = _sessionExercises

    private val _restRemaining = MutableStateFlow(0)
    val restRemaining: StateFlow<Int> = _restRemaining
    private var restJob: Job? = null

    private val _settings = MutableStateFlow(prefs.load())
    val settings: StateFlow<Settings> = _settings

    private val _importing = MutableStateFlow(false)
    val importing: StateFlow<Boolean> = _importing

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    init {
        viewModelScope.launch {
            if (dao.exerciseCount() == 0) dao.insertExercises(SeedData.exercises)
            val open = dao.openSession()
            if (open != null) {
                _activeSessionId.value = open.id
                _activeStart.value = open.startTime
                _sessionExercises.value = dao.exerciseIdsOfSession(open.id)
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    // ---------- Sesi latihan ----------

    fun startSession(template: String? = null) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val id = dao.insertSession(WorkoutSession(startTime = now))
            _activeSessionId.value = id
            _activeStart.value = now
            val names = SeedData.templates[template] ?: emptyList()
            val byName = exercises.value.associateBy { it.name }
            _sessionExercises.value = names.mapNotNull { byName[it]?.id }
        }
    }

    fun addExerciseToSession(exerciseId: Long) {
        if (exerciseId !in _sessionExercises.value) {
            _sessionExercises.value = _sessionExercises.value + exerciseId
        }
    }

    fun removeExerciseFromSession(exerciseId: Long) {
        val sid = _activeSessionId.value ?: return
        viewModelScope.launch {
            allSets.value.filter { it.sessionId == sid && it.exerciseId == exerciseId }
                .forEach { dao.deleteSet(it.id) }
            _sessionExercises.value = _sessionExercises.value - exerciseId
        }
    }

    fun addSet(exerciseId: Long, weight: Double, reps: Int) {
        val sid = _activeSessionId.value ?: return
        viewModelScope.launch {
            dao.insertSet(
                SetEntry(
                    sessionId = sid,
                    exerciseId = exerciseId,
                    weight = weight,
                    reps = reps,
                    timestamp = System.currentTimeMillis()
                )
            )
            addExerciseToSession(exerciseId)
            startRest(_settings.value.restSeconds)
        }
    }

    fun deleteSet(id: Long) {
        viewModelScope.launch { dao.deleteSet(id) }
    }

    fun finishSession() {
        val sid = _activeSessionId.value ?: return
        viewModelScope.launch {
            if (dao.setCount(sid) == 0) {
                dao.deleteSession(sid)
                _message.value = "Sesi kosong dibuang"
            } else {
                dao.endSession(sid, System.currentTimeMillis())
                _message.value = "Sesi selesai. Kerja bagus!"
            }
            resetActive()
        }
    }

    fun discardSession() {
        val sid = _activeSessionId.value ?: return
        viewModelScope.launch {
            dao.deleteSetsOfSession(sid)
            dao.deleteSession(sid)
            resetActive()
        }
    }

    private fun resetActive() {
        _activeSessionId.value = null
        _activeStart.value = 0L
        _sessionExercises.value = emptyList()
        skipRest()
    }

    private fun startRest(seconds: Int) {
        restJob?.cancel()
        restJob = viewModelScope.launch {
            for (s in seconds downTo 1) {
                _restRemaining.value = s
                delay(1000)
            }
            _restRemaining.value = 0
        }
    }

    fun skipRest() {
        restJob?.cancel()
        _restRemaining.value = 0
    }

    /** Set dari sesi sebelumnya (bukan sesi aktif) untuk latihan ini, urut waktu. */
    suspend fun previousSets(exerciseId: Long): List<SetEntry> {
        val recent = dao.recentSets(exerciseId, _activeSessionId.value ?: -1L)
        val sid = recent.firstOrNull()?.sessionId ?: return emptyList()
        return recent.filter { it.sessionId == sid }.sortedBy { it.timestamp }
    }

    // ---------- Library & video ----------

    fun addCustomExercise(name: String, muscle: String, equipment: String) {
        viewModelScope.launch {
            dao.insertExercise(
                Exercise(name = name.trim(), muscle = muscle, equipment = equipment.trim(), isCustom = true)
            )
        }
    }

    fun deleteExercise(exerciseId: Long) {
        viewModelScope.launch {
            val ex = dao.exercise(exerciseId) ?: return@launch
            ex.videoPath?.let { File(it).delete() }
            dao.deleteSetsOfExercise(exerciseId)
            dao.deleteExercise(exerciseId)
            _sessionExercises.value = _sessionExercises.value - exerciseId
        }
    }

    fun saveNote(exerciseId: Long, note: String) {
        viewModelScope.launch {
            val ex = dao.exercise(exerciseId) ?: return@launch
            dao.updateExercise(ex.copy(note = note))
        }
    }

    /** Menyalin video pilihan pengguna ke penyimpanan internal aplikasi supaya tetap tersedia offline. */
    fun importVideo(exerciseId: Long, uri: Uri) {
        viewModelScope.launch {
            _importing.value = true
            try {
                val path = withContext(Dispatchers.IO) {
                    val ctx = getApplication<Application>()
                    val mime = ctx.contentResolver.getType(uri)
                    val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime) ?: "mp4"
                    val dir = File(ctx.filesDir, "videos").apply { mkdirs() }
                    val out = File(dir, "ex_${exerciseId}_${System.currentTimeMillis()}.$ext")
                    val input = ctx.contentResolver.openInputStream(uri)
                        ?: throw IllegalStateException("File tidak bisa dibuka")
                    input.use { src -> out.outputStream().use { dst -> src.copyTo(dst) } }
                    out.absolutePath
                }
                val ex = dao.exercise(exerciseId)
                if (ex != null) {
                    ex.videoPath?.let { File(it).delete() }
                    dao.updateExercise(ex.copy(videoPath = path))
                }
                _message.value = "Video tutorial tersimpan"
            } catch (e: Exception) {
                _message.value = "Gagal mengimpor video: ${e.message}"
            } finally {
                _importing.value = false
            }
        }
    }

    fun removeVideo(exerciseId: Long) {
        viewModelScope.launch {
            val ex = dao.exercise(exerciseId) ?: return@launch
            ex.videoPath?.let { File(it).delete() }
            dao.updateExercise(ex.copy(videoPath = null))
        }
    }

    // ---------- Tubuh ----------

    fun addBody(weight: Double, waist: Double?, arm: Double?, chest: Double?) {
        viewModelScope.launch {
            val day = LocalDate.now().toEpochDay()
            dao.deleteBodyDay(day)
            dao.insertBody(BodyEntry(epochDay = day, weightKg = weight, waistCm = waist, armCm = arm, chestCm = chest))
        }
    }

    fun deleteBody(id: Long) {
        viewModelScope.launch { dao.deleteBody(id) }
    }

    // ---------- Pengaturan ----------

    fun updateSettings(s: Settings) {
        prefs.save(s)
        _settings.value = s
        val ctx = getApplication<Application>()
        if (s.reminderEnabled) {
            ReminderScheduler.schedule(ctx, s.reminderHour, s.reminderMinute)
        } else {
            ReminderScheduler.cancel(ctx)
        }
    }
}
