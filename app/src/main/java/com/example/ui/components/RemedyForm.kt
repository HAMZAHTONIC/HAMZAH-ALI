package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.RemedyEntity

@Composable
fun RemedyForm(
    initialRemedy: RemedyEntity? = null,
    onSave: (RemedyEntity) -> Unit,
    onCancel: () -> Unit,
    submitLabel: String = "Save Remedy"
) {
    var name by remember { mutableStateOf(initialRemedy?.name ?: "") }
    var sourceMaterial by remember { mutableStateOf(initialRemedy?.source_material ?: "") }
    var fullProfile by remember { mutableStateOf(initialRemedy?.full_profile ?: "") }
    var indications by remember { mutableStateOf(initialRemedy?.indications ?: "") }
    var reasoning by remember { mutableStateOf(initialRemedy?.reasoning ?: "") }
    var benefits by remember { mutableStateOf(initialRemedy?.benefits ?: "") }
    var sourceReference by remember { mutableStateOf(initialRemedy?.source_reference ?: "") }

    val isValid = name.isNotBlank() && fullProfile.isNotBlank()

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("remedy_form_card")
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = if (initialRemedy == null) "Add New Remedy" else "Edit Remedy",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Name
            Text("Remedy Name *", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("e.g., Aconitum Napellus") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("remedy_name_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
            )

            // Source Material
            Text("Source Material", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = sourceMaterial,
                onValueChange = { sourceMaterial = it },
                placeholder = { Text("e.g., Whole plant in flower") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("remedy_source_mat_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
            )

            // Full Profile
            Text("Full Remedy Profile *", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = fullProfile,
                onValueChange = { fullProfile = it },
                placeholder = { Text("Paste the complete documentation text") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(bottom = 12.dp)
                    .testTag("remedy_full_profile_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
            )

            // Indications
            Text("Indications / Symptoms", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = indications,
                onValueChange = { indications = it },
                placeholder = { Text("Symptoms and indications associated with this remedy") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .padding(bottom = 12.dp)
                    .testTag("remedy_indications_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
            )

            // Reasoning
            Text("Reasoning", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = reasoning,
                onValueChange = { reasoning = it },
                placeholder = { Text("Why this remedy corresponds to particular symptoms") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .padding(bottom = 12.dp)
                    .testTag("remedy_reasoning_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
            )

            // Benefits
            Text("Expected Benefits", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = benefits,
                onValueChange = { benefits = it },
                placeholder = { Text("Expected/general benefits according to documentation") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .padding(bottom = 12.dp)
                    .testTag("remedy_benefits_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
            )

            // Source Reference
            Text("Source Reference", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = sourceReference,
                onValueChange = { sourceReference = it },
                placeholder = { Text("e.g., Boericke, Clarke") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .testTag("remedy_source_ref_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                LargeButton(
                    text = "Cancel",
                    onClick = onCancel,
                    variant = ButtonVariant.Outline,
                    modifier = Modifier.weight(1f),
                    minHeight = 52.dp,
                    testTag = "remedy_form_cancel_btn"
                )

                LargeButton(
                    text = submitLabel,
                    onClick = {
                        if (isValid) {
                            val remedy = RemedyEntity(
                                id = initialRemedy?.id ?: 0,
                                name = name.trim(),
                                source_material = sourceMaterial.trim().ifEmpty { null },
                                full_profile = fullProfile.trim(),
                                indications = indications.trim().ifEmpty { null },
                                reasoning = reasoning.trim().ifEmpty { null },
                                benefits = benefits.trim().ifEmpty { null },
                                source_reference = sourceReference.trim().ifEmpty { null }
                            )
                            onSave(remedy)
                        }
                    },
                    enabled = isValid,
                    modifier = Modifier.weight(1f),
                    minHeight = 52.dp,
                    testTag = "remedy_form_save_btn"
                )
            }
        }
    }
}
