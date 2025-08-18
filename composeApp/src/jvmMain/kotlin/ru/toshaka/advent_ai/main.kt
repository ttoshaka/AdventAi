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
        LaunchedEffect(Unit) { viewModel.messages.collectLatest { messages = it } }
        App(
            messages = messages,
            onSendMessage = { viewModel.onClick(it) }
        )
    }
}