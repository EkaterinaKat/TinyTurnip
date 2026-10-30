package com.example.tinyturnip.presentation.charts

import androidx.annotation.ColorInt

/**
 * One sector of [PieChartView]; angles are in degrees, clockwise from 3 o'clock as in Canvas.
 * [label] is shown next to the [color] marker in [PieChartLegendView].
 */
data class PieSlice(
    val startAngle: Float,
    val sweepAngle: Float,
    @ColorInt val color: Int,
    val label: String,
)
