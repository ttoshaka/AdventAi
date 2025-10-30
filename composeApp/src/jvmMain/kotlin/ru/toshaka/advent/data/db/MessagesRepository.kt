package ru.toshaka.advent.data.db

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.toshaka.advent.ui.ChatItem

class MessagesRepository(
    private val messageDao: MessageDao,
) {

    suspend fun save(message: MessageEntity) {
        messageDao.insert(message)
    }

    fun getAllAsFlow(chatId: String): Flow<List<ChatItem>> =
        messageDao.getAllAsFlow().map { messages ->
            messages.filter { it.chatId == chatId }.map { it.toItem() }
        }

    suspend fun getAll(chatId: String): List<ChatItem> =
        messageDao.getAll().filter { it.chatId == chatId && it.history }.map { it.toItem() }

    suspend fun clear() {
        messageDao.clearAllMessages()
    }

    suspend fun clearHistory() {
        messageDao.clearHistory()
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