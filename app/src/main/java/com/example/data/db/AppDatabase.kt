package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        RemedyEntity::class,
        ConsultationEntity::class,
        SourceEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun remedyDao(): RemedyDao
    abstract fun consultationDao(): ConsultationDao
    abstract fun sourceDao(): SourceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "homeoclinic_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    database.remedyDao().insertAll(PreseedData.remedies)
                                    database.sourceDao().insertAll(PreseedData.initialSources)
                                }
                            }
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    // Sync remedies safely (add missing items without removing existing ones)
                                    val existingRemedies = database.remedyDao().getAllRemediesSync()
                                    val existingNames = existingRemedies.map { it.name.lowercase().trim() }.toSet()
                                    val missingRemedies = PreseedData.remedies.filter { it.name.lowercase().trim() !in existingNames }
                                    if (missingRemedies.isNotEmpty()) {
                                        database.remedyDao().insertAll(missingRemedies)
                                    }

                                    // Sync sources safely
                                    val existingSources = database.sourceDao().getAllSourcesSync()
                                    val existingTitles = existingSources.map { it.title.lowercase().trim() }.toSet()
                                    val missingSources = PreseedData.initialSources.filter { it.title.lowercase().trim() !in existingTitles }
                                    if (missingSources.isNotEmpty()) {
                                        database.sourceDao().insertAll(missingSources)
                                    }
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
