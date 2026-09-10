package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QuestionCard(
    question: String,
    questionType: String, // yes_no, multiple_choice, text
    options: List<String>?,
    onAnswer: (String) -> Unit,
    onReadAloud: (String) -> Unit,
    loading: Boolean,
    questionNumber: Int,
    isListening: Boolean = false,
    onStartVoice: () -> Unit = {},
    onStopVoice: () -> Unit = {},
    voiceText: String = ""
) {
    var textAnswer by remember { mutableStateOf("") }

    if (voiceText.isNotBlank()) {
        textAnswer = voiceText
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_question_card")
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question $questionNumber",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onReadAloud(question) }
                        .padding(8.dp)
                        .testTag("read_aloud_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Read Aloud",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Read aloud",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = question,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 30.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (loading) {
                LoadingIndicator(message = "Analyzing your answer...")
            } else {
                when (questionType) {
                    "yes_no" -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LargeButton(
                                text = "Yes",
                                onClick = { onAnswer("Yes") },
                                modifier = Modifier.weight(1f),
                                minHeight = 60.dp,
                                testTag = "ai_yes_btn"
                            )

                            LargeButton(
                                text = "No",
                                onClick = { onAnswer("No") },
                                variant = ButtonVariant.Outline,
                                modifier = Modifier.weight(1f),
                                minHeight = 60.dp,
                                testTag = "ai_no_btn"
                            )
                        }
                    }

                    "multiple_choice" -> {
                        val validOptions = options ?: listOf("Yes", "No", "Unsure")
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            validOptions.forEach { opt ->
                                LargeButton(
                                    text = opt,
                                    onClick = { onAnswer(opt) },
                                    variant = ButtonVariant.Secondary,
                                    minHeight = 56.dp,
                                    testTag = "ai_mc_${opt.lowercase().replace(" ", "_")}"
                                )
                            }
                        }
                    }

                    else -> { // text
                        Column {
                            OutlinedTextField(
                                value = textAnswer,
                                onValueChange = { textAnswer = it },
                                placeholder = { Text("Type your answer here...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .testTag("ai_text_input"),
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
                                    text = "Submit Answer",
                                    onClick = {
                                        if (textAnswer.isNotBlank()) {
                                            onAnswer(textAnswer.trim())
                                            textAnswer = ""
                                        }
                                    },
                                    enabled = textAnswer.isNotBlank(),
                                    modifier = Modifier.weight(1f).padding(start = 12.dp),
                                    minHeight = 56.dp,
                                    testTag = "ai_submit_answer_btn"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
