package ru.toshaka.advent_ai.day2

import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.flow.collectLatest
import ru.toshaka.advent_ai.day2.model.Element

fun main() = application {
    val viewModel = MainViewModel()
    Window(
        onCloseRequest = ::exitApplication,
        title = "AdventAi",
    ) {
        var textElements by remember { mutableStateOf(emptyList<Element>()) }
        LaunchedEffect(Unit) {
            viewModel.textElements.collectLatest { textElements = it }
        }
        App(
            elements = textElements,
            onSendMessage = { viewModel.onClick(it) }
        )
    }
}