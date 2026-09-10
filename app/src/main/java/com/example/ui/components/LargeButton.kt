package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DestructiveRed

enum class ButtonVariant {
    Primary, Secondary, Outline, Danger
}

@Composable
fun LargeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    enabled: Boolean = true,
    minHeight: Dp = 64.dp,
    icon: (@Composable () -> Unit)? = null,
    testTag: String = ""
) {
    val shape = RoundedCornerShape(16.dp)
    val testTagModifier = if (testTag.isNotEmpty()) modifier.testTag(testTag) else modifier

    when (variant) {
        ButtonVariant.Primary -> {
            Button(
                onClick = onClick,
                enabled = enabled,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = testTagModifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = minHeight)
            ) {
                ButtonContent(text = text, icon = icon)
            }
        }
        ButtonVariant.Secondary -> {
            Button(
                onClick = onClick,
                enabled = enabled,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = testTagModifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = minHeight)
            ) {
                ButtonContent(text = text, icon = icon)
            }
        }
        ButtonVariant.Outline -> {
            OutlinedButton(
                onClick = onClick,
                enabled = enabled,
                shape = shape,
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = testTagModifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = minHeight)
            ) {
                ButtonContent(text = text, icon = icon)
            }
        }
        ButtonVariant.Danger -> {
            Button(
                onClick = onClick,
                enabled = enabled,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DestructiveRed,
                    contentColor = Color.White
                ),
                modifier = testTagModifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = minHeight)
            ) {
                ButtonContent(text = text, icon = icon)
            }
        }
    }
}

@Composable
private fun ButtonContent(text: String, icon: (@Composable () -> Unit)?) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        if (icon != null) {
            icon()
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(horizontal = 6.dp))
        }
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
