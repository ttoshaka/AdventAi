package ru.toshaka.advent.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun App(
    messages: List<ChatItem>,
    onSendClick: (String) -> Unit,
) {
    MaterialTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            messages.forEach { messageItem ->
                when (messageItem) {
                    is ChatItem.ChatMessage -> {
                        ChatMessageItem(
                            authorName = messageItem.authorName,
                            messageText = messageItem.messageText,
                            debugInfo = messageItem.debugInfo,
                            isOwnMessage = messageItem.isOwnMessage,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            ChatInputBar(
                onSendClick = onSendClick,
            )
        }
    }
}

@Composable
private fun ChatMessageItem(
    authorName: String,
    messageText: String,
    debugInfo: String?,
    isOwnMessage: Boolean = false,
    maxWidthFraction: Float = 0.5f,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = if (isOwnMessage) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        val messageMaxWidth = maxWidth * maxWidthFraction

        Column(
            modifier = Modifier
                .widthIn(max = messageMaxWidth)
                .background(
                    color = if (isOwnMessage) Color(0xFFDFFFD6) else Color(0xFFFFFFFF),
                    shape = RoundedCornerShape(12.dp)
                )
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Text(
                text = authorName,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = messageText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
            debugInfo?.also {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = debugInfo,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    onSendClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var message by remember { mutableStateOf("") }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(24.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = message,
            onValueChange = { message = it },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.Black),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Send,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (message.isNotBlank()) {
                        onSendClick(message)
                        message = ""
                    }
                }
            ),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            decorationBox = { innerTextField ->
                if (message.isEmpty()) {
                    Text(
                        text = "Введите сообщение...",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                innerTextField()
            }
        )

        Text(
            text = "➤",
            color = if (message.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray,
            modifier = Modifier
                .padding(start = 8.dp)
                .clickable(enabled = message.isNotBlank()) {
                    onSendClick(message)
                    message = ""
                },
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )
    }
}