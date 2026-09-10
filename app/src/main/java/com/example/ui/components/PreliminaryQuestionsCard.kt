package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PreliminaryAnswer

data class PrelimStepData(
    val key: String,
    val label: String,
    val question: String,
    val type: String, // text, multiple_choice, yes_no
    val options: List<String> = emptyList()
)

val PRELIM_STEPS = listOf(
    PrelimStepData(
        key = "complaint",
        label = "Main reason for consultation",
        question = "What is the main reason for your consultation today?",
        type = "text"
    ),
    PrelimStepData(
        key = "onset",
        label = "When the problem started",
        question = "When did this problem start?",
        type = "multiple_choice",
        options = listOf("Today", "A few days ago", "About a week ago", "Longer than a week ago")
    ),
    PrelimStepData(
        key = "progression",
        label = "Progression (better/worse/same)",
        question = "Is the problem getting better, worse, or staying the same?",
        type = "multiple_choice",
        options = listOf("Getting better", "Getting worse", "Staying the same")
    ),
    PrelimStepData(
        key = "existing_conditions",
        label = "Existing health conditions",
        question = "Do you have any existing health conditions?",
        type = "yes_no"
    ),
    PrelimStepData(
        key = "current_medicines",
        label = "Current medicines or treatments",
        question = "Are you currently taking any medicines or treatments?",
        type = "yes_no"
    ),
    PrelimStepData(
        key = "allergies",
        label = "Allergies or sensitivities",
        question = "Do you have any important allergies or sensitivities?",
        type = "yes_no"
    )
)

@Composable
fun PreliminaryQuestionsCard(
    onComplete: (List<PreliminaryAnswer>, String) -> Unit,
    onBackToHome: () -> Unit,
    isListening: Boolean = false,
    onStartVoice: () -> Unit = {},
    onStopVoice: () -> Unit = {},
    voiceText: String = ""
) {
    var currentStep by remember { mutableIntStateOf(0) }
    var textInput by remember { mutableStateOf("") }
    val answers = remember { mutableStateListOf<PreliminaryAnswer>() }

    // Sync voice recognition text into text input if on text step
    if (voiceText.isNotBlank() && currentStep == 0) {
        textInput = voiceText
    }

    val stepData = PRELIM_STEPS[currentStep]
    val progress = (currentStep + 1) / 6f

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("preliminary_questions_card")
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Step ${currentStep + 1} of 6",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${((currentStep + 1) * 100) / 6}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Question text
            Text(
                text = stepData.question,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 30.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Inputs based on type
            when (stepData.type) {
                "text" -> {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Describe your health concern in your own words...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("prelim_text_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VoiceInputButton(
                            isListening = isListening,
                            onStartListening = onStartVoice,
                            onStopListening = onStopVoice
                        )

                        LargeButton(
                            text = if (currentStep == 5) "Submit" else "Continue",
                            onClick = {
                                if (textInput.isNotBlank()) {
                                    val newAns = PreliminaryAnswer(stepData.key, stepData.label, textInput.trim())
                                    answers.add(newAns)
                                    val complaintText = answers.find { it.questionKey == "complaint" }?.answer ?: textInput.trim()
                                    if (currentStep < 5) {
                                        currentStep++
                                        textInput = ""
                                    } else {
                                        onComplete(answers.toList(), complaintText)
                                    }
                                }
                            },
                            enabled = textInput.isNotBlank(),
                            modifier = Modifier.weight(1f).padding(start = 12.dp),
                            minHeight = 56.dp,
                            testTag = "prelim_continue_btn"
                        )
                    }
                }

                "multiple_choice" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        stepData.options.forEach { option ->
                            LargeButton(
                                text = option,
                                onClick = {
                                    answers.add(PreliminaryAnswer(stepData.key, stepData.label, option))
                                    if (currentStep < 5) {
                                        currentStep++
                                    } else {
                                        val complaintText = answers.find { it.questionKey == "complaint" }?.answer ?: ""
                                        onComplete(answers.toList(), complaintText)
                                    }
                                },
                                variant = ButtonVariant.Secondary,
                                minHeight = 56.dp,
                                testTag = "prelim_mc_${option.lowercase().replace(" ", "_")}"
                            )
                        }
                    }
                }

                "yes_no" -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LargeButton(
                            text = "Yes",
                            onClick = {
                                answers.add(PreliminaryAnswer(stepData.key, stepData.label, "Yes"))
                                if (currentStep < 5) {
                                    currentStep++
                                } else {
                                    val complaintText = answers.find { it.questionKey == "complaint" }?.answer ?: ""
                                    onComplete(answers.toList(), complaintText)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            minHeight = 60.dp,
                            testTag = "prelim_yes_btn"
                        )

                        LargeButton(
                            text = "No",
                            onClick = {
                                answers.add(PreliminaryAnswer(stepData.key, stepData.label, "No"))
                                if (currentStep < 5) {
                                    currentStep++
                                } else {
                                    val complaintText = answers.find { it.questionKey == "complaint" }?.answer ?: ""
                                    onComplete(answers.toList(), complaintText)
                                }
                            },
                            variant = ButtonVariant.Outline,
                            modifier = Modifier.weight(1f),
                            minHeight = 60.dp,
                            testTag = "prelim_no_btn"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            LargeButton(
                text = "Back to Home",
                onClick = onBackToHome,
                variant = ButtonVariant.Outline,
                minHeight = 52.dp,
                testTag = "prelim_back_home"
            )
        }
    }
}
