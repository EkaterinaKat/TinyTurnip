package com.example.tinyturnip.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.tinyturnip.data.local.entity.LogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Query("SELECT * FROM logs ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<LogEntity>>

    @Query("SELECT * FROM logs WHERE act_id = :actId ORDER BY timestamp DESC")
    fun observeForAct(actId: Long): Flow<List<LogEntity>>

    @Query("SELECT * FROM logs WHERE timestamp >= :from AND timestamp < :to ORDER BY timestamp")
    suspend fun getBetween(from: Long, to: Long): List<LogEntity>

    @Query("SELECT * FROM logs ORDER BY id DESC LIMIT 1")
    suspend fun getLatest(): LogEntity?

    @Query("SELECT MAX(timestamp) FROM logs")
    suspend fun getMaxTimestamp(): Long?

    @Query("SELECT COUNT(*) FROM logs WHERE act_id = :actId")
    suspend fun countForAct(actId: Long): Int

    @Insert
    suspend fun insert(log: LogEntity): Long

    @Delete
    suspend fun delete(log: LogEntity)

    @Query("DELETE FROM logs WHERE id = :id")
    suspend fun deleteById(id: Long): Int
}
