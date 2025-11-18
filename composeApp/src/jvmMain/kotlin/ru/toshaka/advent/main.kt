package ru.toshaka.advent

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import ru.toshaka.advent.ui.MainViewModel
import ru.toshaka.advent.ui.view.App

fun main() = application {
    val viewModel = MainViewModel()
    val state by viewModel.state.collectAsState()
    Window(
        onCloseRequest = ::exitApplication,
        title = "AdventAi_3",
        state = WindowState(
            width = 1200.dp,
            height = 900.dp,
        ),
    ) {
        App(state)
    }
}