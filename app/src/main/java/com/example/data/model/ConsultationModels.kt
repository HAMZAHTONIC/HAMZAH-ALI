package com.example.data.model

data class PreliminaryAnswer(
    val questionKey: String,
    val questionLabel: String,
    val answer: String
)

data class QaItem(
    val question: String,
    val answer: String,
    val questionType: String = "text"
)

data class MatchDetail(
    val patient_symptom: String,
    val database_info: String,
    val match_reason: String
)

data class ConsultationAiResponse(
    val action: String? = "no_match", // ask_question, provide_recommendation, no_match
    val question: String? = null,
    val question_type: String? = "text", // yes_no, multiple_choice, text
    val options: List<String>? = null,
    val medicine_name: String? = null,
    val why: String? = null,
    val match_details: List<MatchDetail>? = null,
    val confidence_level: String? = null, // strong, partial, insufficient
    val benefit: String? = null,
    val source: String? = null,
    val has_red_flag: Boolean? = false,
    val red_flag_message: String? = null,
    val message: String? = null
)

data class ChatMessage(
    val isUser: Boolean,
    val content: String
)
