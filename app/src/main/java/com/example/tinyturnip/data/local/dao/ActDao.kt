package com.example.tinyturnip.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.tinyturnip.data.local.entity.ActEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActDao {
    @Query("SELECT * FROM acts ORDER BY name")
    fun observeAll(): Flow<List<ActEntity>>

    @Query("SELECT * FROM acts WHERE id = :id")
    suspend fun getById(id: Long): ActEntity?

    @Insert
    suspend fun insert(act: ActEntity): Long

    @Update
    suspend fun update(act: ActEntity)

    @Delete
    suspend fun delete(act: ActEntity)

    @Query("DELETE FROM acts WHERE id = :id")
    suspend fun deleteById(id: Long): Int
}
