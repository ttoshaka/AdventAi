package ru.toshaka.advent.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChatInputBar(
    modifier: Modifier = Modifier,
    onSendClick: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    val canSend = text.isNotBlank()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(24.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = text,
            onValueChange = { text = it },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.Black),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Send,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                onSend = { if (canSend) sendMessage(text, onSendClick) { text = "" } }
            ),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            decorationBox = { inner ->
                if (text.isEmpty()) {
                    Text(
                        text = "Введите сообщение...",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                inner()
            }
        )

        SendButton(enabled = canSend) {
            sendMessage(text, onSendClick) { text = "" }
        }
    }
}

@Composable
private fun SendButton(enabled: Boolean, onClick: () -> Unit) {
    val color = if (enabled) MaterialTheme.colorScheme.primary else Color.Gray

    Text(
        text = "➤",
        color = color,
        fontSize = 20.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .padding(start = 8.dp)
            .clickable(enabled = enabled) { onClick() }
    )
}

private inline fun sendMessage(text: String, onSendClick: (String) -> Unit, onClear: () -> Unit) {
    if (text.isNotBlank()) {
        onSendClick(text.trim())
        onClear()
    }
}
