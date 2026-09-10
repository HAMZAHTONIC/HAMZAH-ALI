package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DestructiveContainer
import com.example.ui.theme.DestructiveRed

@Composable
fun RedFlagWarning(message: String?) {
    val context = LocalContext.current
    val alertText = message ?: "If you are experiencing severe or life-threatening symptoms, seek immediate emergency medical care."

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DestructiveContainer)
            .border(2.dp, DestructiveRed, RoundedCornerShape(20.dp))
            .padding(20.dp)
            .testTag("red_flag_warning")
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(DestructiveRed)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Emergency Red Flag Warning",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Important Warning",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DestructiveRed,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = alertText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = DestructiveRed,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(DestructiveRed)
                    .clickable { dialEmergency(context) }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("call_emergency_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Call 911",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Call Emergency Services (911)",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

private fun dialEmergency(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:911"))
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
