package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RemedyDao {
    @Query("SELECT * FROM remedies ORDER BY name ASC")
    fun getAllRemedies(): Flow<List<RemedyEntity>>

    @Query("SELECT * FROM remedies ORDER BY name ASC")
    suspend fun getAllRemediesSync(): List<RemedyEntity>

    @Query("SELECT * FROM remedies WHERE id = :id LIMIT 1")
    suspend fun getRemedyById(id: Int): RemedyEntity?

    @Query("SELECT * FROM remedies WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getRemedyByName(name: String): RemedyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRemedy(remedy: RemedyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remedies: List<RemedyEntity>)

    @Update
    suspend fun updateRemedy(remedy: RemedyEntity)

    @Query("DELETE FROM remedies WHERE id = :id")
    suspend fun deleteRemedyById(id: Int)

    @Query("SELECT COUNT(*) FROM remedies")
    suspend fun getRemedyCount(): Int
}
