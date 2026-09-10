package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.speech.SpeechToTextHelper
import com.example.speech.TextToSpeechHelper
import com.example.ui.components.ButtonVariant
import com.example.ui.components.LargeButton
import com.example.ui.components.LoadingIndicator
import com.example.ui.components.PreliminaryQuestionsCard
import com.example.ui.components.QuestionCard
import com.example.ui.viewmodel.ConsultationPhase
import com.example.ui.viewmodel.ConsultationViewModel

@Composable
fun ConsultationScreen(
    viewModel: ConsultationViewModel,
    onNavigateHome: () -> Unit,
    onNavigateResult: (Int) -> Unit
) {
    val context = LocalContext.current
    val phase by viewModel.phase.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentAiResponse by viewModel.currentAiResponse.collectAsState()
    val qaHistory by viewModel.qaHistory.collectAsState()

    val sttHelper = remember { SpeechToTextHelper(context) }
    val ttsHelper = remember { TextToSpeechHelper(context) }

    val isListening by sttHelper.isListening.collectAsState()
    val sttError by sttHelper.errorMessage.collectAsState()
    var voiceText by remember { mutableStateOf("") }

    LaunchedEffect(sttError) {
        sttError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            sttHelper.clearError()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            sttHelper.stopListening()
            ttsHelper.shutdown()
        }
    }

    // React to completed phase
    LaunchedEffect(phase) {
        if (phase is ConsultationPhase.Completed) {
            val id = (phase as ConsultationPhase.Completed).consultationId
            onNavigateResult(id)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        when (phase) {
            is ConsultationPhase.Preliminary -> {
                PreliminaryQuestionsCard(
                    onComplete = { answers, mainComplaint ->
                        viewModel.submitPreliminary(answers, mainComplaint)
                    },
                    onBackToHome = onNavigateHome,
                    isListening = isListening,
                    onStartVoice = {
                        sttHelper.startListening { transcript ->
                            voiceText = transcript
                        }
                    },
                    onStopVoice = { sttHelper.stopListening() },
                    voiceText = voiceText
                )
            }

            is ConsultationPhase.Questions -> {
                LargeButton(
                    text = "Back to Home",
                    onClick = onNavigateHome,
                    variant = ButtonVariant.Outline,
                    minHeight = 50.dp,
                    testTag = "ai_q_back_home"
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    LoadingIndicator(message = "Analyzing your answers...")
                } else {
                    val resp = currentAiResponse
                    if (resp != null && resp.question != null) {
                        QuestionCard(
                            question = resp.question,
                            questionType = resp.question_type ?: "text",
                            options = resp.options,
                            onAnswer = { answer ->
                                voiceText = ""
                                viewModel.answerCurrentQuestion(answer)
                            },
                            onReadAloud = { qText ->
                                ttsHelper.speak(qText)
                            },
                            loading = isLoading,
                            questionNumber = qaHistory.size + 1,
                            isListening = isListening,
                            onStartVoice = {
                                sttHelper.startListening { transcript ->
                                    voiceText = transcript
                                }
                            },
                            onStopVoice = { sttHelper.stopListening() },
                            voiceText = voiceText
                        )
                    } else {
                        LoadingIndicator(message = "Preparing consultation...")
                    }
                }
            }

            is ConsultationPhase.GeneratingResult -> {
                LoadingIndicator(message = "Generating your recommendation...")
            }

            is ConsultationPhase.Completed -> {
                LoadingIndicator(message = "Loading result...")
            }
        }
    }
}
