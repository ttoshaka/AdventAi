package ru.toshaka.advent

import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.toshaka.advent.data.model.DeepSeekApi

fun main() = application {
    val deepSeekApi = DeepSeekApi()
    val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val scope = CoroutineScope(Dispatchers.IO)
    Window(
        onCloseRequest = ::exitApplication,
        title = "AdventAi_3",
    ) {
        var messages by remember { mutableStateOf(emptyList<ChatMessage>()) }
        LaunchedEffect(Unit) { _messages.asStateFlow().collectLatest { messages = it } }
        App(
            messages = messages,
            onSendClick = {
                _messages.value += ChatMessage(
                    authorName = "Me",
                    messageText = it,
                    debugInfo = null,
                    isOwnMessage = true
                )
                scope.launch {
                    val response = deepSeekApi.sendChat(it)
                    _messages.value += ChatMessage(
                        authorName = "Ai",
                        messageText = response.choices.first().message.content,
                        debugInfo = response.usage?.promptTokens.toString(),
                        isOwnMessage = false
                    )
                }
            }
        )
    }
}