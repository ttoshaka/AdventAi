package ru.toshaka.advent

sealed interface ChatItem {

    data class ChatMessage(
        val authorName: String,
        val messageText: String,
        val debugInfo: String?,
        val isOwnMessage: Boolean = false,
    ) : ChatItem

    data class ChatJoke(
        val question: String,
        val answer: String,
        val authorName: String,
    ) : ChatItem
}