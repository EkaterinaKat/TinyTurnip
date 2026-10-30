package com.example.tinyturnip.domain.charts

import com.example.tinyturnip.data.local.entity.LogEntity
import com.example.tinyturnip.data.local.entity.LogType
import com.example.tinyturnip.data.repository.ActRepository
import com.example.tinyturnip.data.repository.LogRepository
import java.util.concurrent.TimeUnit

class ChartService(
    private val logRepository: LogRepository,
    private val actRepository: ActRepository,
) {

    /**
     * Calculates chart data for the given local day; [month] is 1-based (1 = January).
     * Logs must strictly alternate start, finish, start, ... and end with a finish,
     * otherwise [ChartResult.Error] is returned.
     */
    suspend fun calculate(day: Int, month: Int, year: Int): ChartResult {
        val logs = logRepository.getByDate(day, month, year).sortedBy { it.timestamp }
        if (logs.isEmpty()) return ChartResult.NoData
        if (!isConsistent(logs)) return ChartResult.Error(INVALID_DATA_MESSAGE)

        val millisByActId = mutableMapOf<Long, Long>()
        var totalMillis = 0L
        for ((start, finish) in logs.chunked(2)) {
            val duration = finish.timestamp - start.timestamp
            millisByActId.merge(start.actId!!, duration, Long::plus)
            totalMillis += duration
        }

        val minutesByAct = millisByActId.mapKeys { (actId, _) ->
            actRepository.getById(actId) ?: return ChartResult.Error(INVALID_DATA_MESSAGE)
        }.mapValues { (_, millis) -> millis.toMinutes() }

        return ChartResult.Success(
            ChartData(
                firstStartTime = logs.first().timestamp,
                spanMinutes = (logs.last().timestamp - logs.first().timestamp).toMinutes(),
                minutesByAct = minutesByAct,
                totalMinutes = totalMillis.toMinutes(),
            )
        )
    }

    /** Even positions are starts with an act, odd positions are finishes, and the last log is a finish. */
    private fun isConsistent(logs: List<LogEntity>): Boolean =
        logs.size % 2 == 0 && logs.withIndex().all { (index, log) ->
            if (index % 2 == 0) log.logType == LogType.ACT_START && log.actId != null
            else log.logType == LogType.FINISH
        }

    private fun Long.toMinutes(): Int = TimeUnit.MILLISECONDS.toMinutes(this).toInt()

    private companion object {
        const val INVALID_DATA_MESSAGE = "Некорректные данные"
    }
}
