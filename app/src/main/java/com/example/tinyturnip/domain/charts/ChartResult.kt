package com.example.tinyturnip.domain.charts

sealed interface ChartResult {

    data class Success(val data: ChartData) : ChartResult

    /** There are no logs for the requested day. */
    data object NoData : ChartResult

    data class Error(val message: String) : ChartResult
}
