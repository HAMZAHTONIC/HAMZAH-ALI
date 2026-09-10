package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remedies")
data class RemedyEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val source_material: String? = null,
    val full_profile: String,
    val indications: String? = null,
    val reasoning: String? = null,
    val benefits: String? = null,
    val source_reference: String? = null
)
