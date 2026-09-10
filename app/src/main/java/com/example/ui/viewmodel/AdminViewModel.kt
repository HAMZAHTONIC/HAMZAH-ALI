package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.RemedyEntity
import com.example.data.db.SourceEntity
import com.example.data.repository.AdminAuthRepository
import com.example.data.repository.RemedyRepository
import com.example.data.repository.SourceRepository
import com.example.utils.DocExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val remedyRepo = RemedyRepository(db.remedyDao())
    private val sourceRepo = SourceRepository(db.sourceDao())
    private val authRepo = AdminAuthRepository()

    val isAuthenticated: StateFlow<Boolean> = authRepo.isAuthenticated

    val remedies: StateFlow<List<RemedyEntity>> = remedyRepo.allRemedies
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allSources: StateFlow<List<SourceEntity>> = sourceRepo.allSources
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _showAddForm = MutableStateFlow(false)
    val showAddForm: StateFlow<Boolean> = _showAddForm.asStateFlow()

    private val _editingRemedy = MutableStateFlow<RemedyEntity?>(null)
    val editingRemedy: StateFlow<RemedyEntity?> = _editingRemedy.asStateFlow()

    private val _expandedRemedyId = MutableStateFlow<Int?>(null)
    val expandedRemedyId: StateFlow<Int?> = _expandedRemedyId.asStateFlow()

    fun authenticatePasskey(passkey: String): Boolean {
        return authRepo.authenticate(passkey)
    }

    fun toggleAddForm(show: Boolean) {
        _showAddForm.value = show
        if (show) _editingRemedy.value = null
    }

    fun setEditingRemedy(remedy: RemedyEntity?) {
        _editingRemedy.value = remedy
        if (remedy != null) _showAddForm.value = false
    }

    fun toggleExpandRemedy(id: Int) {
        _expandedRemedyId.value = if (_expandedRemedyId.value == id) null else id
    }

    fun deleteSource(id: Int) {
        viewModelScope.launch {
            sourceRepo.deleteSource(id)
        }
    }

    fun saveRemedy(remedy: RemedyEntity) {
        viewModelScope.launch {
            if (remedy.id == 0) {
                remedyRepo.insertRemedy(remedy)
            } else {
                remedyRepo.updateRemedy(remedy)
            }
            _showAddForm.value = false
            _editingRemedy.value = null
        }
    }

    fun deleteRemedy(id: Int) {
        viewModelScope.launch {
            remedyRepo.deleteRemedyById(id)
            if (_expandedRemedyId.value == id) _expandedRemedyId.value = null
        }
    }

    fun exportWordDoc() {
        viewModelScope.launch {
            val list = remedyRepo.getAllRemediesSync()
            DocExporter.exportAndShareWordDoc(getApplication(), list)
        }
    }
}
