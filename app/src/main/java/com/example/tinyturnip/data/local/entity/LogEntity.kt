package com.example.tinyturnip.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "logs",
    foreignKeys = [
        ForeignKey(
            entity = ActEntity::class,
            parentColumns = ["id"],
            childColumns = ["act_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("act_id")],
)
data class LogEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Set for [LogType.ACT_START], always null for [LogType.FINISH]. */
    @ColumnInfo(name = "act_id")
    val actId: Long? = null,

    @ColumnInfo(name = "log_type")
    val logType: LogType,

    val timestamp: Long = System.currentTimeMillis(),

)
