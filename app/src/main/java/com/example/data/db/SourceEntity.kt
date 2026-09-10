package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sources")
data class SourceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val author: String? = null,
    val publisher: String? = null,
    val publication_year: Int? = null,
    val edition: String? = null,
    val isbn: String? = null,
    val url: String? = null,
    val source_type: String = "Original Book", // Original Book, Digitized Book, Journal Paper, Library Catalog, Traditional Reference, Web Article
    val date_accessed: Long = System.currentTimeMillis(),
    val last_verified_date: Long = System.currentTimeMillis(),
    val verification_status: String = "VERIFIED", // VERIFIED, UNVERIFIED, POTENTIALLY_FORGED, SOURCE_UNAVAILABLE
    val evidence_classification: String = "Historical Homeopathic Literature", // Historical Homeopathic Literature, Materia Medica Description, Traditional Homeopathic Claim, Modern Scientific Research, Clinical Study, Systematic Review
    val confidence_score: Float = 0.95f,
    val authenticity_notes: String? = null,
    val supporting_sources_json: String? = null,
    val contradicting_sources_json: String? = null,
    val is_broken: Boolean = false
)
