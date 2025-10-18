package ru.toshaka.advent.ui

import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.flow.collectLatest

fun main() = application {
    val viewModel = MainViewModel()
    Window(
        onCloseRequest = ::exitApplication,
        title = "AdventAi_3",
    ) {
        var messages by remember { mutableStateOf(emptyList<ChatItem>()) }
        LaunchedEffect(Unit) { viewModel.chatItems.collectLatest { messages = it } }
        App(
            messages = messages,
            onSendClick = viewModel::onSendMessageClick,
        )
    }
}