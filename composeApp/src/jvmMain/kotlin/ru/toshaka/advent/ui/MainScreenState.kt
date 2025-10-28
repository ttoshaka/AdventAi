package ru.toshaka.advent.ui

import androidx.compose.ui.graphics.Color

data class MainScreenState(
    val chats: Map<String, Chat>
) {
    data class Chat(
        val name: String,
        val messages: List<Message>,
        val onSendClick: (String) -> Unit,
        val onClearClick: () -> Unit,
    ) {
        data class Message(
            val author: String,
            val content: String,
            val position: Position,
            val debug: String?,
            val color: Color
        ) {
            enum class Position {
                LEFT, RIGHT
            }
        }
    }

    companion object {
        val Empty = MainScreenState(mutableMapOf())
    }
}