package com.example.data.model

data class ResearchDiscoveryResult(
    val remedyName: String,
    val sourceTitle: String,
    val sourceAuthor: String? = null,
    val sourcePublisher: String? = null,
    val sourceYear: Int? = null,
    val edition: String? = null,
    val isbn: String? = null,
    val sourceUrl: String? = null,
    val sourceType: String = "Digitized Book",
    val verificationStatus: String = "UNVERIFIED", // VERIFIED, UNVERIFIED, POTENTIALLY_FORGED
    val evidenceClassification: String = "Materia Medica Description",
    val confidenceScore: Float = 0.8f,
    val authenticityAuditNotes: String = "",
    val extractedProfile: String = "",
    val extractedSymptoms: String = "",
    val extractedModalities: String? = null,
    val extractedClaims: String? = null,
    val hasConflicts: Boolean = false,
    val conflictDetails: String? = null,
    val supportingSources: List<String> = emptyList(),
    val contradictingSources: List<String> = emptyList()
)
