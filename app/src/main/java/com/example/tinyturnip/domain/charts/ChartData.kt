package com.example.tinyturnip.domain.charts

import com.example.tinyturnip.data.local.entity.ActEntity

/** Aggregated activity data for one day, used to build charts. */
data class ChartData(

    /** Timestamp of the first [com.example.tinyturnip.data.local.entity.LogType.ACT_START] log. */
    val firstStartTime: Long,

    /** Minutes between the first start and the last finish. */
    val spanMinutes: Int,

    /** Tracked minutes per act. */
    val minutesByAct: Map<ActEntity, Int>,

    /** Sum of all start–finish intervals in minutes. */
    val totalMinutes: Int,

)
