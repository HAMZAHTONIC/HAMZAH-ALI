package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "consultations")
data class ConsultationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patient_session_id: String,
    val complaint: String,
    val preliminary_answers_json: String? = null,
    val qa_history_json: String? = null,
    val status: String = "in_progress", // in_progress, completed, no_match, escalated
    val recommended_remedy: String? = null,
    val explanation: String? = null,
    val match_details_json: String? = null,
    val confidence_level: String? = null, // strong, partial, insufficient
    val benefit: String? = null,
    val source_reference: String? = null,
    val has_red_flag: Boolean = false,
    val red_flag_message: String? = null,
    val created_date: Long = System.currentTimeMillis()
)
