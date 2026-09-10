package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speech.SpeechToTextHelper
import com.example.ui.components.ButtonVariant
import com.example.ui.components.LargeButton
import com.example.ui.components.LoadingIndicator
import com.example.ui.components.RedFlagWarning
import com.example.ui.components.ResultCard
import com.example.ui.components.VoiceInputButton
import com.example.ui.viewmodel.ConsultationViewModel

@Composable
fun ResultScreen(
    consultationId: Int,
    viewModel: ConsultationViewModel,
    onNewConsultation: () -> Unit,
    onReturnHome: () -> Unit
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val consultation by viewModel.activeConsultation.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isSendingChat by viewModel.isSendingChat.collectAsState()

    var followUpText by remember { mutableStateOf("") }

    val sttHelper = remember { SpeechToTextHelper(context) }
    val isListening by sttHelper.isListening.collectAsState()
    val sttError by sttHelper.errorMessage.collectAsState()

    LaunchedEffect(consultationId) {
        viewModel.loadConsultationById(consultationId)
    }

    LaunchedEffect(sttError) {
        sttError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            sttHelper.clearError()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            sttHelper.stopListening()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        if (isLoading) {
            LoadingIndicator(message = "Loading your result...")
        } else if (consultation == null) {
            // Not Found
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("result_not_found_card")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Not Found",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Could not find your consultation.",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    LargeButton(
                        text = "Return Home",
                        onClick = onReturnHome,
                        testTag = "result_not_found_home_btn"
                    )
                }
            }
        } else {
            val item = consultation!!

            if (item.status == "no_match" || item.recommended_remedy.isNullOrEmpty()) {
                // No Match Screen
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("no_match_card")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(28.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "No Match",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "No Match Found",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = item.red_flag_message ?: "I was unable to find a suitable medicine in my database based on the information provided. We recommend consulting a qualified homeopathic practitioner for a thorough evaluation.",
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        LargeButton(
                            text = "New Consultation",
                            onClick = onNewConsultation,
                            icon = { Icon(Icons.Default.Refresh, null) },
                            testTag = "no_match_new_btn"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LargeButton(
                            text = "Return Home",
                            onClick = onReturnHome,
                            variant = ButtonVariant.Outline,
                            icon = { Icon(Icons.Default.Home, null) },
                            testTag = "no_match_home_btn"
                        )
                    }
                }
            } else {
                // Completed Recommendation
                if (item.has_red_flag) {
                    RedFlagWarning(message = item.red_flag_message)
                    Spacer(modifier = Modifier.height(20.dp))
                }

                val matchDetails = viewModel.deserializeMatchDetails(item.match_details_json)

                ResultCard(
                    medicineName = item.recommended_remedy ?: "",
                    why = item.explanation,
                    benefit = item.benefit,
                    source = item.source_reference,
                    matchDetails = matchDetails,
                    confidenceLevel = item.confidence_level
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Follow-up Q&A Chat Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("follow_up_chat_card")
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.QuestionAnswer,
                                contentDescription = "Chat",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Ask a Follow-up Question",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Chat history
                        chatMessages.forEach { msg ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                contentAlignment = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 16.dp,
                                                topEnd = 16.dp,
                                                bottomStart = if (msg.isUser) 16.dp else 4.dp,
                                                bottomEnd = if (msg.isUser) 4.dp else 16.dp
                                            )
                                        )
                                        .background(
                                            if (msg.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
                                        )
                                        .padding(14.dp)
                                ) {
                                    Text(
                                        text = msg.content,
                                        color = if (msg.isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 16.sp,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }

                        if (isSendingChat) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Thinking...",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = followUpText,
                            onValueChange = { followUpText = it },
                            placeholder = { Text("Type your question about this recommendation...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .testTag("follow_up_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            VoiceInputButton(
                                isListening = isListening,
                                onStartListening = {
                                    sttHelper.startListening { transcript ->
                                        followUpText = transcript
                                    }
                                },
                                onStopListening = { sttHelper.stopListening() },
                                label = "Speak"
                            )

                            LargeButton(
                                text = "Send",
                                onClick = {
                                    if (followUpText.isNotBlank()) {
                                        viewModel.sendFollowUpMessage(followUpText.trim())
                                        followUpText = ""
                                    }
                                },
                                enabled = followUpText.isNotBlank() && !isSendingChat,
                                modifier = Modifier.weight(1f).padding(start = 12.dp),
                                minHeight = 52.dp,
                                icon = { Icon(Icons.Default.Send, null) },
                                testTag = "follow_up_send_btn"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                LargeButton(
                    text = "New Consultation",
                    onClick = onNewConsultation,
                    icon = { Icon(Icons.Default.Refresh, null) },
                    testTag = "result_new_consultation_btn"
                )

                Spacer(modifier = Modifier.height(12.dp))

                LargeButton(
                    text = "Return Home",
                    onClick = onReturnHome,
                    variant = ButtonVariant.Outline,
                    icon = { Icon(Icons.Default.Home, null) },
                    testTag = "result_return_home_btn"
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
