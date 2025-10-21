package ru.toshaka.advent.data.db

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.toshaka.advent.ui.ChatItem

class MessagesRepository(
    private val messageDao: MessageDao,
) {

    suspend fun save(chatItem: ChatItem, chatId: Long) {
        val message = when (chatItem) {
            is ChatItem.ChatMessage -> MessageEntity(
                content = chatItem.messageText,
                author = chatItem.authorName,
                role = if (chatItem.isOwnMessage) MessageEntity.Roles.user else MessageEntity.Roles.assistant,
                chatId = chatId,
                debugInfo = chatItem.debugInfo,
            )
        }
        messageDao.insert(message)
    }

    fun getAllAsFlow(id: Long): Flow<List<ChatItem>> =
        messageDao.getAllAsFlow().map { messages ->
            messages.filter { it.chatId == id }.map { it.toItem() }
        }

    suspend fun getAll(chatId: Long): List<ChatItem> =
        messageDao.getAll().filter { it.chatId == chatId }.map { it.toItem() }

    suspend fun clear() {
        messageDao.clearAllMessages()
    }

    private fun MessageEntity.toItem(): ChatItem =
        ChatItem.ChatMessage(
            authorName = author,
            messageText = content,
            debugInfo = debugInfo,
            isOwnMessage = when (role) {
                MessageEntity.Roles.user -> true
                MessageEntity.Roles.assistant -> false
            },
        )
}