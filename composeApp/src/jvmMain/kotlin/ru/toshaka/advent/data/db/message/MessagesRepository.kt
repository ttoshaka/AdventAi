package ru.toshaka.advent.data.db.message

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MessagesRepository(
    private val messageDao: MessageDao,
) {

    suspend fun save(message: MessageEntity) {
        messageDao.insert(message)
    }

    fun getAllAsFlow(chatId: Long): Flow<List<MessageEntity>> =
        messageDao.getAllAsFlow().map { messages ->
            messages.filter { it.chatId == chatId }
        }

    fun getAllAsFlow(): Flow<List<MessageEntity>> =
        messageDao.getAllAsFlow()

    suspend fun getAll(chatId: Long): List<MessageEntity> =
        messageDao.getAll().filter { it.chatId == chatId }
        //listOf(
        //    MessageEntity(
        //        id = 1L,
        //        chatId = 0L,
        //        owner = 0,
        //        content = "Моё первое сообщение",
        //        debugInfo = ""
        //    ),
//
        //    MessageEntity(
        //        id = 4L,
        //        chatId = 0L,
        //        owner = 1,
        //        content = "Первое сообщение первого агента",
        //        debugInfo = ""
        //    ),
//
        //    MessageEntity(
        //        id = 2L,
        //        chatId = 0L,
        //        owner = 0,
        //        content = "Моё второе сообщение",
        //        debugInfo = ""
        //    ),
//
        //    MessageEntity(
        //        id = 5L,
        //        chatId = 0L,
        //        owner = 1,
        //        content = "Второе сообщение первого агента",
        //        debugInfo = ""
        //    ),
//
        //    MessageEntity(
        //        id = 3L,
        //        chatId = 0L,
        //        owner = 0,
        //        content = "Моё третье сообщение",
        //        debugInfo = ""
        //    ),
//
        //    MessageEntity(
        //        id = 6L,
        //        chatId = 0L,
        //        owner = 2,
        //        content = "Первое сообщение второго агента",
        //        debugInfo = ""
        //    ),
        //)

    suspend fun clear() {
        messageDao.clearAllMessages()
    }

    suspend fun clearHistory() {
        messageDao.clearHistory()
    }
}