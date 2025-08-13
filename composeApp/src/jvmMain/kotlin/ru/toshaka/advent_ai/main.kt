package ru.toshaka.advent_ai

import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.flow.collectLatest
import ru.toshaka.advent_ai.model.Element
import ru.toshaka.advent_ai.model.DisplayedMessage

fun main() = application {
    val viewModel = MainViewModel()
    Window(
        onCloseRequest = ::exitApplication,
        title = "AdventAi",
    ) {
        var messages by remember { mutableStateOf(emptyList<DisplayedMessage>()) }
        var elements by remember { mutableStateOf(emptyList<Element>()) }
        LaunchedEffect(Unit) {
            viewModel.elements.collectLatest {
                println("Collect = $it")
                elements = it
            }
        }

        LaunchedEffect(Unit) {
            viewModel.messages.collectLatest {
                println("Collect = $it")
                messages = it
            }
        }
        App(
            messages = messages,
            elements = elements,
            onSendMessage = { viewModel.onClick(it) }
        )
    }
}