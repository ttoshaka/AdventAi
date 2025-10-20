package ru.toshaka.advent.ui

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import ru.toshaka.advent.data.DeepSeekApi
import ru.toshaka.advent.data.db.AppDatabase
import ru.toshaka.advent.data.db.MessagesRepository
import ru.toshaka.advent.data.model.Type
import java.io.File

class MainViewModel {

    private val deepSeekApi = DeepSeekApi()
    private val viewModelScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val json = Json {
        classDiscriminator = "type"
    }
    private val database = getRoomDatabase()
    private val messageRepository = MessagesRepository(database.getDao())

    val chatItems get() = messageRepository.getAllAsFlow()

    fun onSendMessageClick(text: String) {
        val item = ChatItem.ChatMessage(
            authorName = "Me",
            messageText = text,
            debugInfo = null,
            isOwnMessage = true
        )
        viewModelScope.launch {
            addChatItem(item)
            val previousMessages = messageRepository.getAll().map {
                when (it) {
                    is ChatItem.ChatMessage -> it.messageText to if (it.isOwnMessage) "user" else "assistant"
                }
            }
            val response = deepSeekApi.sendChat(previousMessages).choices.first().message.content
            val message = json.decodeFromString<Type>(response).toChatItem()
            addChatItem(message)
        }
    }

    fun onClearClick() {
        viewModelScope.launch {
            messageRepository.clear()
        }
    }

    private suspend fun addChatItem(item: ChatItem) {
        messageRepository.save(item)
    }

    private fun getRoomDatabase(): AppDatabase {
        return Room.databaseBuilder<AppDatabase>(
            name = File(
                System.getProperty("java.io.tmpdir"),
                "my_room.db"
            ).absolutePath
        )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
}