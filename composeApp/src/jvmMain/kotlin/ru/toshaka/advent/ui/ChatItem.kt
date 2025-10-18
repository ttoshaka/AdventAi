package ru.toshaka.advent.ui

sealed interface ChatItem {

    data class ChatMessage(
        val authorName: String,
        val messageText: String,
        val debugInfo: String?,
        val isOwnMessage: Boolean = false,
    ) : ChatItem
}