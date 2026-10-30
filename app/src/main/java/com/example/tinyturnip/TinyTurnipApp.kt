package com.example.tinyturnip

import android.app.Application
import com.example.tinyturnip.data.local.AppDatabase
import com.example.tinyturnip.data.repository.ActRepository
import com.example.tinyturnip.data.repository.LogRepository
import com.example.tinyturnip.domain.charts.ChartService

class TinyTurnipApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val actRepository: ActRepository by lazy { ActRepository(database) }
    val logRepository: LogRepository by lazy { LogRepository(database) }
    val chartService: ChartService by lazy { ChartService(logRepository, actRepository) }
}
