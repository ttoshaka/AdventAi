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
import kotlinx.serialization.json.Json
import ru.toshaka.advent.data.QaModel
import ru.toshaka.advent.data.model.DeepSeekApi

fun main() = application {
    val deepSeekApi = DeepSeekApi()
    val _messages = MutableStateFlow<List<ChatItem>>(emptyList())
    val scope = CoroutineScope(Dispatchers.IO)
    Window(
        onCloseRequest = ::exitApplication,
        title = "AdventAi_3",
    ) {
        var messages by remember { mutableStateOf(emptyList<ChatItem>()) }
        LaunchedEffect(Unit) { _messages.asStateFlow().collectLatest { messages = it } }
        App(
            messages = messages,
            onSendClick = {
                _messages.value += ChatItem.ChatMessage(
                    authorName = "Me",
                    messageText = it,
                    debugInfo = null,
                    isOwnMessage = true
                )
                scope.launch {
                    val response = deepSeekApi.sendChat(it).choices.first().message.content.parse()
                    _messages.value += ChatItem.ChatJoke(
                        question = response.question,
                        answer = response.answer,
                        authorName = "Ai"
                    )
                }
            }
        )
    }
}

private fun String.parse(): QaModel {
    return Json.decodeFromString<QaModel>(this)
}