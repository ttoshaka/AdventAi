package ru.toshaka.advent.ui

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import ru.toshaka.advent.data.agent.DeepSeekChatAgent
import ru.toshaka.advent.data.db.AppDatabase
import ru.toshaka.advent.data.db.MessagesRepository
import java.io.File

fun main() = application {
    val database = getRoomDatabase()
    val messageRepository = MessagesRepository(database.getDao())

    val viewModel = MainViewModel(
        agent = DeepSeekChatAgent {
            name = "Default agent"
            systemPrompt = "Ты AI-ассистент."
            history = messageHistory("Default agent", messageRepository)
        },
        messageRepository = messageRepository,
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "AdventAi_3",
    ) {
        App(listOf(viewModel))
    }
}

private fun messageHistory(chatId: String, messageRepository: MessagesRepository): () -> List<Pair<String, String>> = {
    runBlocking {
        messageRepository.getAll(chatId).map {
            when (it) {
                is ChatItem.ChatMessage -> run { if (it.isOwnMessage) "user" else "assistant" } to it.messageText
            }
        }
    }
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