package com.example.tinyturnip.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.tinyturnip.data.local.dao.ActDao
import com.example.tinyturnip.data.local.dao.LogDao
import com.example.tinyturnip.data.local.entity.ActEntity
import com.example.tinyturnip.data.local.entity.LogEntity

@Database(
    entities = [ActEntity::class, LogEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun actDao(): ActDao
    abstract fun logDao(): LogDao

    companion object {
        private const val DATABASE_NAME = "tinyturnip.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME,
                )
                    // No migrations: on a version change all tables are dropped and recreated
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
    }
}
