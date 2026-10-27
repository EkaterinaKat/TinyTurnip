package com.example.tinyturnip.presentation.logs

import com.example.tinyturnip.data.local.entity.ActEntity
import com.example.tinyturnip.data.local.entity.LogEntity
import java.util.Calendar

sealed interface LogListItem {
    /** [dayStart] is the local midnight of the day, in millis. */
    data class DateHeader(val dayStart: Long) : LogListItem

    data class Log(val log: LogEntity, val actName: String) : LogListItem

    companion object {
        /**
         * Groups logs by local day: days go from newest to oldest,
         * logs inside a day go from oldest to newest.
         */
        fun build(logs: List<LogEntity>, acts: List<ActEntity>): List<LogListItem> {
            val actNames = acts.associate { it.id to it.name }
            val calendar = Calendar.getInstance()
            return logs
                .sortedByDescending { it.timestamp }
                .groupBy { dayStart(calendar, it.timestamp) }
                .flatMap { (dayStart, dayLogs) ->
                    listOf(DateHeader(dayStart)) +
                        dayLogs.asReversed().map { Log(it, it.actId?.let(actNames::get).orEmpty()) }
                }
        }

        private fun dayStart(calendar: Calendar, timestamp: Long): Long {
            calendar.timeInMillis = timestamp
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar.timeInMillis
        }
    }
}
