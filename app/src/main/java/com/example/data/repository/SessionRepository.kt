package com.example.data.repository

import android.content.Context
import java.util.UUID

class SessionRepository(context: Context) {
    private val prefs = context.getSharedPreferences("homeoclinic_prefs", Context.MODE_PRIVATE)

    fun getPatientSessionId(): String {
        var sessionId = prefs.getString("patient_session_id", null)
        if (sessionId.isNull_or_empty()) {
            sessionId = UUID.randomUUID().toString()
            prefs.edit().putString("patient_session_id", sessionId).apply()
        }
        return sessionId ?: UUID.randomUUID().toString()
    }

    private fun String?.isNull_or_empty(): Boolean = this == null || this.isEmpty()
}
