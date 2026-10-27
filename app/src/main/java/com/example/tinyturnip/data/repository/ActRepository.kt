package com.example.tinyturnip.data.repository

import androidx.room.withTransaction
import com.example.tinyturnip.data.local.AppDatabase
import com.example.tinyturnip.data.local.entity.ActEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.Collator

class ActRepository(private val database: AppDatabase) {

    private val actDao = database.actDao()
    private val logDao = database.logDao()

    /** Returns id of the created act. */
    suspend fun create(name: String, description: String): Long =
        actDao.insert(ActEntity(name = name, description = description))

    suspend fun getById(id: Long): ActEntity? = actDao.getById(id)

    /**
     * Acts sorted alphabetically by name, case-insensitive and locale-aware
     * (SQLite's NOCASE only folds ASCII, so Cyrillic names would sort by case).
     */
    fun getAll(): Flow<List<ActEntity>> = actDao.observeAll().map { acts ->
        val collator = Collator.getInstance().apply { strength = Collator.SECONDARY }
        acts.sortedWith(compareBy(collator) { it.name })
    }

    suspend fun update(act: ActEntity) = actDao.update(act)

    /**
     * Deletes the act only if no logs reference it.
     * Returns true if the act was deleted.
     */
    suspend fun delete(id: Long): Boolean = database.withTransaction {
        if (logDao.countForAct(id) > 0) return@withTransaction false
        actDao.deleteById(id) > 0
    }
}
