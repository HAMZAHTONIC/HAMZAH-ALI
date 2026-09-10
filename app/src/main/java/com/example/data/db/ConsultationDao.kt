package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ConsultationDao {
    @Query("SELECT * FROM consultations WHERE patient_session_id = :sessionId ORDER BY created_date DESC LIMIT :limit")
    fun getConsultationsBySession(sessionId: String, limit: Int = 50): Flow<List<ConsultationEntity>>

    @Query("SELECT * FROM consultations WHERE id = :id LIMIT 1")
    fun getConsultationById(id: Int): Flow<ConsultationEntity?>

    @Query("SELECT * FROM consultations WHERE id = :id LIMIT 1")
    suspend fun getConsultationByIdSync(id: Int): ConsultationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsultation(consultation: ConsultationEntity): Long

    @Update
    suspend fun updateConsultation(consultation: ConsultationEntity)
}
