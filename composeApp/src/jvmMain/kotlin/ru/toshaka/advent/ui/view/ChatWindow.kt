package ru.toshaka.advent.ui.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.toshaka.advent.ui.MainScreenState

@Composable
fun ChatWindow(
    chat: MainScreenState.Chat,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val messageCount = chat.messages.size
    LaunchedEffect(messageCount) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        //ChatHeader(chat.name)
        Spacer(Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 8.dp)
        ) {
            chat.messages.forEach { message ->
                ChatMessageBubble(message)
            }
        }
        Spacer(Modifier.height(4.dp))
        ChatFooter(chat)
    }
}

@Composable
private fun ChatHeader(title: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
private fun ChatFooter(chat: MainScreenState.Chat) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChatInputBar(
                modifier = Modifier.weight(1f),
                onSendClick = chat.onSendClick
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = chat.onClearClick) {
                Text("Очистить", fontSize = 13.sp)
            }
            Button(onClick = { chat.onSummarizeClick(chat.id) }) {
                Text("Суммаризация", fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(message: MainScreenState.Chat.Message) {
    val alignment = when (message.position) {
        MainScreenState.Chat.Message.Position.LEFT -> Alignment.CenterStart
        MainScreenState.Chat.Message.Position.RIGHT -> Alignment.CenterEnd
    }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        contentAlignment = alignment
    ) {
        // Динамическая ширина: чуть меньше половины ширины экрана
        val bubbleMaxWidth = maxWidth * 0.45f

        Column(
            modifier = Modifier
                .widthIn(min = 100.dp, max = bubbleMaxWidth) // минимум и максимум
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .border(0.5.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                message.author,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.Gray
            )
            Spacer(Modifier.height(4.dp))
            Text(
                message.content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            message.debug?.let { debug ->
                Spacer(Modifier.height(6.dp))
                Text(
                    text = debug,
                    fontSize = 11.sp,
                    color = Color.Gray.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

