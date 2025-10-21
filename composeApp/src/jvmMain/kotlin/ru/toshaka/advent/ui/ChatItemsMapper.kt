package ru.toshaka.advent.ui

import ru.toshaka.advent.data.model.Type

fun Type.toChatItem(debugInf: String? = null): ChatItem =
    when (this) {
        is Type.Text -> ChatItem.ChatMessage(
            authorName = "Ai",
            messageText = content,
            debugInfo = debugInf,
            isOwnMessage = false,
        )

        is Type.Question -> ChatItem.ChatMessage(
            authorName = "Ai",
            messageText = content,
            debugInfo = debugInf,
            isOwnMessage = false,
        )
    }