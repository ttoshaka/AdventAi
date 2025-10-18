package ru.toshaka.advent.data.db

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.toshaka.advent.ui.ChatItem

class MessagesRepository(
    private val messageDao: MessageDao,
) {

    suspend fun save(chatItem: ChatItem) {
        val message = when (chatItem) {
            is ChatItem.ChatMessage -> MessageEntity(
                content = chatItem.messageText,
                author = chatItem.authorName,
                isOwnMessage = chatItem.isOwnMessage,
            )
        }
        messageDao.insert(message)
    }

    fun getAll(): Flow<List<ChatItem>> =
        messageDao.getAll().map { messages ->
            messages.map { message ->
                ChatItem.ChatMessage(
                    authorName = message.author,
                    messageText = message.content,
                    debugInfo = "From DB",
                    isOwnMessage = message.isOwnMessage,
                )
            }
        }

    suspend fun clear() {
        messageDao.clearAllMessages()
    }
}