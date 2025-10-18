package ru.toshaka.advent.ui

import ru.toshaka.advent.data.model.Type

fun Type.toChatItem(): ChatItem =
    when (this) {
        is Type.Text -> ChatItem.ChatMessage(
            authorName = "Ai",
            messageText = content,
            debugInfo = this.javaClass.name,
            isOwnMessage = false,
        )

        is Type.Question -> ChatItem.ChatMessage(
            authorName = "Ai",
            messageText = content,
            debugInfo = this.javaClass.name,
            isOwnMessage = false,
        )
    }