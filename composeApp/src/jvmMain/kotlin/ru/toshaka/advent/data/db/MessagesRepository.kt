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
                role = if (chatItem.isOwnMessage) MessageEntity.Roles.user else MessageEntity.Roles.assistant,
            )
        }
        messageDao.insert(message)
    }

    fun getAllAsFlow(): Flow<List<ChatItem>> =
        messageDao.getAllAsFlow().map { messages ->
            messages.map { it.toItem() }
        }

    suspend fun getAll(): List<ChatItem> =
        messageDao.getAll().map { it.toItem() }

    suspend fun clear() {
        messageDao.clearAllMessages()
    }

    private fun MessageEntity.toItem(): ChatItem =
        ChatItem.ChatMessage(
            authorName = author,
            messageText = content,
            debugInfo = "From DB",
            isOwnMessage = when (role) {
                MessageEntity.Roles.user -> true
                MessageEntity.Roles.assistant -> false
            },
        )
}