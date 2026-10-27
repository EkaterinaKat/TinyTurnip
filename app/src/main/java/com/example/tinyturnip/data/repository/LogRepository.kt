package com.example.tinyturnip.data.repository

import androidx.room.withTransaction
import com.example.tinyturnip.data.local.AppDatabase
import com.example.tinyturnip.data.local.entity.LogEntity
import com.example.tinyturnip.data.local.entity.LogType
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class LogRepository(private val database: AppDatabase) {

    private val logDao = database.logDao()

    /** Creates an [LogType.ACT_START] log for the act. Returns id of the created log. */
    suspend fun createActStart(actId: Long): Long =
        logDao.insert(LogEntity(actId = actId, logType = LogType.ACT_START))

    /** Creates a [LogType.FINISH] log, which never references an act. Returns id of the created log. */
    suspend fun createFinish(): Long =
        logDao.insert(LogEntity(logType = LogType.FINISH))

    /**
     * Creates a log with an explicit [timestamp]; [actId] is used only for [LogType.ACT_START].
     * The log is created only if it is later than every existing log.
     * Returns true if the log was created.
     */
    suspend fun createManual(logType: LogType, actId: Long?, timestamp: Long): Boolean =
        database.withTransaction {
            val maxTimestamp = logDao.getMaxTimestamp()
            if (maxTimestamp != null && timestamp <= maxTimestamp) return@withTransaction false
            val log = LogEntity(
                actId = if (logType == LogType.ACT_START) actId else null,
                logType = logType,
                timestamp = timestamp,
            )
            logDao.insert(log)
            true
        }

    /** All logs, newest first. */
    fun getAll(): Flow<List<LogEntity>> = logDao.observeAll()

    /** Logs for the given local day; [month] is 1-based (1 = January). */
    suspend fun getByDate(day: Int, month: Int, year: Int): List<LogEntity> {
        val calendar = Calendar.getInstance().apply {
            clear()
            set(year, month - 1, day)
        }
        val from = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        return logDao.getBetween(from, calendar.timeInMillis)
    }

    /** The most recently added log, or null if there are none. */
    suspend fun getLatest(): LogEntity? = logDao.getLatest()

    /**
     * Deletes the log only if it is the most recently added one.
     * Returns true if the log was deleted.
     */
    suspend fun delete(id: Long): Boolean = database.withTransaction {
        if (logDao.getLatest()?.id != id) return@withTransaction false
        logDao.deleteById(id) > 0
    }
}
