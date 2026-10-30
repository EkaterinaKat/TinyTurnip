package com.example.tinyturnip.presentation.charts

import com.example.tinyturnip.data.local.entity.ActEntity

/**
 * Turns minutes per act into [PieSlice]s for [PieChartView]; [colorFor] returns a color int,
 * [labelFor] builds the legend text from an act and its minutes.
 */
class PieChartBuilder(
    private val colorFor: (ActEntity) -> Int,
    private val labelFor: (act: ActEntity, minutes: Int) -> String,
) {

    /**
     * Slices go clockwise from 12 o'clock, the largest act first.
     * Acts with no tracked minutes are skipped; an empty list is returned if nothing was tracked.
     */
    fun build(minutesByAct: Map<ActEntity, Int>): List<PieSlice> {
        val entries = minutesByAct.filterValues { it > 0 }.entries.sortedByDescending { it.value }
        val total = entries.sumOf { it.value }
        if (total == 0) return emptyList()

        var startAngle = START_ANGLE
        return entries.map { (act, minutes) ->
            val sweepAngle = FULL_CIRCLE * minutes / total
            PieSlice(startAngle, sweepAngle, colorFor(act), labelFor(act, minutes)).also { startAngle += sweepAngle }
        }
    }

    private companion object {
        const val START_ANGLE = -90f
        const val FULL_CIRCLE = 360f
    }
}
