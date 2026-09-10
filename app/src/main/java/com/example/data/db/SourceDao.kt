package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SourceDao {
    @Query("SELECT * FROM sources ORDER BY last_verified_date DESC")
    fun getAllSources(): Flow<List<SourceEntity>>

    @Query("SELECT * FROM sources ORDER BY last_verified_date DESC")
    suspend fun getAllSourcesSync(): List<SourceEntity>

    @Query("SELECT * FROM sources WHERE id = :id LIMIT 1")
    suspend fun getSourceById(id: Int): SourceEntity?

    @Query("SELECT * FROM sources WHERE LOWER(title) LIKE '%' || LOWER(:query) || '%' OR LOWER(author) LIKE '%' || LOWER(:query) || '%' OR LOWER(publisher) LIKE '%' || LOWER(:query) || '%'")
    fun searchSources(query: String): Flow<List<SourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: SourceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sources: List<SourceEntity>)

    @Update
    suspend fun updateSource(source: SourceEntity)

    @Query("DELETE FROM sources WHERE id = :id")
    suspend fun deleteSourceById(id: Int)

    @Query("SELECT COUNT(*) FROM sources")
    suspend fun getSourceCount(): Int
}
