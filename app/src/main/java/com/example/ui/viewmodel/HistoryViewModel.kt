package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.ConsultationEntity
import com.example.data.repository.ConsultationRepository
import com.example.data.repository.SessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val consultationRepo = ConsultationRepository(db.consultationDao())
    private val sessionRepo = SessionRepository(application)

    val patientSessionId: String = sessionRepo.getPatientSessionId()

    val consultations: StateFlow<List<ConsultationEntity>> = consultationRepo
        .getConsultationsBySession(patientSessionId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
