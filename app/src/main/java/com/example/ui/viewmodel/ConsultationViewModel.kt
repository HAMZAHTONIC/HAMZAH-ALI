package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.ConsultationEntity
import com.example.data.db.RemedyEntity
import com.example.data.model.ChatMessage
import com.example.data.model.ConsultationAiResponse
import com.example.data.model.PreliminaryAnswer
import com.example.data.model.QaItem
import com.example.data.network.GeminiApiService
import com.example.data.repository.ConsultationRepository
import com.example.data.repository.RemedyRepository
import com.example.data.repository.SessionRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ConsultationPhase {
    object Preliminary : ConsultationPhase()
    object Questions : ConsultationPhase()
    object GeneratingResult : ConsultationPhase()
    data class Completed(val consultationId: Int) : ConsultationPhase()
}

class ConsultationViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val consultationRepo = ConsultationRepository(db.consultationDao())
    private val remedyRepo = RemedyRepository(db.remedyDao())
    private val sessionRepo = SessionRepository(application)

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    private val _phase = MutableStateFlow<ConsultationPhase>(ConsultationPhase.Preliminary)
    val phase: StateFlow<ConsultationPhase> = _phase.asStateFlow()

    private val _preliminaryAnswers = MutableStateFlow<List<PreliminaryAnswer>>(emptyList())
    val preliminaryAnswers: StateFlow<List<PreliminaryAnswer>> = _preliminaryAnswers.asStateFlow()

    private val _complaint = MutableStateFlow("")
    val complaint: StateFlow<String> = _complaint.asStateFlow()

    private val _qaHistory = MutableStateFlow<List<QaItem>>(emptyList())
    val qaHistory: StateFlow<List<QaItem>> = _qaHistory.asStateFlow()

    private val _currentAiResponse = MutableStateFlow<ConsultationAiResponse?>(null)
    val currentAiResponse: StateFlow<ConsultationAiResponse?> = _currentAiResponse.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Loaded consultation for Result screen
    private val _activeConsultation = MutableStateFlow<ConsultationEntity?>(null)
    val activeConsultation: StateFlow<ConsultationEntity?> = _activeConsultation.asStateFlow()

    private val _activeRemedyProfile = MutableStateFlow<RemedyEntity?>(null)
    val activeRemedyProfile: StateFlow<RemedyEntity?> = _activeRemedyProfile.asStateFlow()

    // Chat messages for follow-up
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isSendingChat = MutableStateFlow(false)
    val isSendingChat: StateFlow<Boolean> = _isSendingChat.asStateFlow()

    fun resetConsultation() {
        _phase.value = ConsultationPhase.Preliminary
        _preliminaryAnswers.value = emptyList()
        _complaint.value = ""
        _qaHistory.value = emptyList()
        _currentAiResponse.value = null
        _isLoading.value = false
        _activeConsultation.value = null
        _activeRemedyProfile.value = null
        _chatMessages.value = emptyList()
    }

    fun submitPreliminary(answers: List<PreliminaryAnswer>, mainComplaint: String) {
        _preliminaryAnswers.value = answers
        _complaint.value = mainComplaint
        _phase.value = ConsultationPhase.Questions
        _isLoading.value = true

        viewModelScope.launch {
            val remedies = remedyRepo.getAllRemediesSync()
            val aiResp = GeminiApiService.getConsultationNextStep(
                complaint = mainComplaint,
                preliminaryAnswers = answers,
                qaHistory = emptyList(),
                remedies = remedies
            )
            processAiResponse(aiResp)
        }
    }

    fun answerCurrentQuestion(answerText: String) {
        val currentQ = _currentAiResponse.value?.question ?: return
        val currentType = _currentAiResponse.value?.question_type ?: "text"

        val updatedHistory = _qaHistory.value + QaItem(
            question = currentQ,
            answer = answerText,
            questionType = currentType
        )
        _qaHistory.value = updatedHistory
        _isLoading.value = true

        viewModelScope.launch {
            val remedies = remedyRepo.getAllRemediesSync()
            val aiResp = GeminiApiService.getConsultationNextStep(
                complaint = _complaint.value,
                preliminaryAnswers = _preliminaryAnswers.value,
                qaHistory = updatedHistory,
                remedies = remedies
            )
            processAiResponse(aiResp)
        }
    }

    private suspend fun processAiResponse(aiResp: ConsultationAiResponse) {
        _isLoading.value = false
        _currentAiResponse.value = aiResp

        when (aiResp.action) {
            "ask_question" -> {
                // Stay on Questions phase with new question
            }
            "provide_recommendation" -> {
                saveConsultationAndComplete("completed", aiResp)
            }
            else -> { // no_match
                saveConsultationAndComplete("no_match", aiResp)
            }
        }
    }

    private suspend fun saveConsultationAndComplete(status: String, aiResp: ConsultationAiResponse) {
        _phase.value = ConsultationPhase.GeneratingResult

        val prelimJson = serializePreliminary(_preliminaryAnswers.value)
        val qaJson = serializeQa(_qaHistory.value)
        val matchJson = aiResp.match_details?.let { serializeMatchDetails(it) }

        val entity = ConsultationEntity(
            patient_session_id = sessionRepo.getPatientSessionId(),
            complaint = _complaint.value,
            preliminary_answers_json = prelimJson,
            qa_history_json = qaJson,
            status = status,
            recommended_remedy = aiResp.medicine_name,
            explanation = aiResp.why,
            match_details_json = matchJson,
            confidence_level = aiResp.confidence_level ?: if (status == "completed") "strong" else "insufficient",
            benefit = aiResp.benefit,
            source_reference = aiResp.source,
            has_red_flag = aiResp.has_red_flag ?: false,
            red_flag_message = aiResp.red_flag_message ?: aiResp.message
        )

        val id = consultationRepo.insertConsultation(entity).toInt()
        _phase.value = ConsultationPhase.Completed(id)
    }

    fun loadConsultationById(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val consultation = consultationRepo.getConsultationByIdSync(id)
            _activeConsultation.value = consultation

            if (consultation?.recommended_remedy != null) {
                val remedy = remedyRepo.getRemedyByName(consultation.recommended_remedy)
                _activeRemedyProfile.value = remedy
            }
            _isLoading.value = false
        }
    }

    fun sendFollowUpMessage(userMsg: String) {
        if (userMsg.isBlank() || _isSendingChat.value) return

        val currentMessages = _chatMessages.value + ChatMessage(isUser = true, content = userMsg)
        _chatMessages.value = currentMessages
        _isSendingChat.value = true

        val consultation = _activeConsultation.value
        val remedy = _activeRemedyProfile.value

        viewModelScope.launch {
            val response = GeminiApiService.getFollowUpAnswer(
                complaint = consultation?.complaint ?: "",
                recommendedRemedy = consultation?.recommended_remedy ?: "",
                explanation = consultation?.explanation ?: "",
                benefit = consultation?.benefit ?: "",
                remedyProfile = remedy?.full_profile ?: "",
                userQuestion = userMsg
            )

            _chatMessages.value = currentMessages + ChatMessage(isUser = false, content = response)
            _isSendingChat.value = false
        }
    }

    private fun serializePreliminary(list: List<PreliminaryAnswer>): String {
        return try {
            val type = Types.newParameterizedType(List::class.java, PreliminaryAnswer::class.java)
            moshi.adapter<List<PreliminaryAnswer>>(type).toJson(list)
        } catch (e: Exception) { "" }
    }

    private fun serializeQa(list: List<QaItem>): String {
        return try {
            val type = Types.newParameterizedType(List::class.java, QaItem::class.java)
            moshi.adapter<List<QaItem>>(type).toJson(list)
        } catch (e: Exception) { "" }
    }

    private fun serializeMatchDetails(list: List<com.example.data.model.MatchDetail>): String {
        return try {
            val type = Types.newParameterizedType(List::class.java, com.example.data.model.MatchDetail::class.java)
            moshi.adapter<List<com.example.data.model.MatchDetail>>(type).toJson(list)
        } catch (e: Exception) { "" }
    }

    fun deserializeMatchDetails(json: String?): List<com.example.data.model.MatchDetail> {
        if (json.isNull_or_empty()) return emptyList()
        return try {
            val type = Types.newParameterizedType(List::class.java, com.example.data.model.MatchDetail::class.java)
            moshi.adapter<List<com.example.data.model.MatchDetail>>(type).fromJson(json) ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    private fun String?.isNull_or_empty(): Boolean = this == null || this.isEmpty()
}
