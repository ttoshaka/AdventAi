package ru.toshaka.advent.data.db.chat

import kotlinx.coroutines.flow.Flow

class ChatRepository(
    private val chatDao: ChatDao,
) {

    suspend fun save(chat: ChatEntity): Long {
        return chatDao.insert(chat)
    }

    fun getAllAsFlow(): Flow<List<ChatEntity>> =
        chatDao.getAllAsFlow()

    suspend fun getAll(): List<ChatEntity> =
        chatDao.getAll()

    suspend fun clear() {
        chatDao.clearAllMessages()
    }

    suspend fun clearHistory() {
        chatDao.clearHistory()
    }
}