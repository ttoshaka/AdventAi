package ru.toshaka.advent

data class ChatMessage(
    val authorName: String,
    val messageText: String,
    val debugInfo: String?,
    val isOwnMessage: Boolean = false,
)