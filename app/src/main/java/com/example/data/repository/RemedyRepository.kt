package com.example.data.repository

import com.example.data.db.RemedyDao
import com.example.data.db.RemedyEntity
import kotlinx.coroutines.flow.Flow

class RemedyRepository(private val remedyDao: RemedyDao) {
    val allRemedies: Flow<List<RemedyEntity>> = remedyDao.getAllRemedies()

    suspend fun getAllRemediesSync(): List<RemedyEntity> = remedyDao.getAllRemediesSync()

    suspend fun getRemedyById(id: Int): RemedyEntity? = remedyDao.getRemedyById(id)

    suspend fun getRemedyByName(name: String): RemedyEntity? = remedyDao.getRemedyByName(name)

    suspend fun insertRemedy(remedy: RemedyEntity): Long = remedyDao.insertRemedy(remedy)

    suspend fun updateRemedy(remedy: RemedyEntity) = remedyDao.updateRemedy(remedy)

    suspend fun deleteRemedyById(id: Int) = remedyDao.deleteRemedyById(id)
}
