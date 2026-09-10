package com.example.data.repository

import com.example.data.db.ConsultationDao
import com.example.data.db.ConsultationEntity
import kotlinx.coroutines.flow.Flow

class ConsultationRepository(private val consultationDao: ConsultationDao) {
    fun getConsultationsBySession(sessionId: String): Flow<List<ConsultationEntity>> {
        return consultationDao.getConsultationsBySession(sessionId)
    }

    fun getConsultationById(id: Int): Flow<ConsultationEntity?> {
        return consultationDao.getConsultationById(id)
    }

    suspend fun getConsultationByIdSync(id: Int): ConsultationEntity? {
        return consultationDao.getConsultationByIdSync(id)
    }

    suspend fun insertConsultation(consultation: ConsultationEntity): Long {
        return consultationDao.insertConsultation(consultation)
    }

    suspend fun updateConsultation(consultation: ConsultationEntity) {
        consultationDao.updateConsultation(consultation)
    }
}
