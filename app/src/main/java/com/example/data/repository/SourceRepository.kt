package com.example.data.repository

import com.example.data.db.SourceDao
import com.example.data.db.SourceEntity
import kotlinx.coroutines.flow.Flow

class SourceRepository(
    private val sourceDao: SourceDao
) {
    val allSources: Flow<List<SourceEntity>> = sourceDao.getAllSources()

    suspend fun insertSource(source: SourceEntity): Long = sourceDao.insertSource(source)

    suspend fun updateSource(source: SourceEntity) = sourceDao.updateSource(source)

    suspend fun deleteSource(id: Int) = sourceDao.deleteSourceById(id)

    fun searchSources(query: String): Flow<List<SourceEntity>> = sourceDao.searchSources(query)
}
