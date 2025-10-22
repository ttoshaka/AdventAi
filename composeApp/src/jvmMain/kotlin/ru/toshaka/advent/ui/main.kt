package ru.toshaka.advent.ui

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import ru.toshaka.advent.data.DeepSeekApi
import ru.toshaka.advent.data.db.AppDatabase
import ru.toshaka.advent.data.db.MessagesRepository
import java.io.File

fun main() = application {
    val deepSeekApi = DeepSeekApi()
    val database = getRoomDatabase()
    val messageRepository = MessagesRepository(database.getDao())


    val viewModels = listOf(
        MainViewModel(1, "meta-llama/Llama-3.3-70B-Instruct", deepSeekApi, messageRepository),
        MainViewModel(2, "openai/gpt-oss-20b", deepSeekApi, messageRepository),
        MainViewModel(3, "moonshotai/Kimi-K2-Instruct-0905", deepSeekApi, messageRepository),
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "AdventAi_3",
    ) {

        App(viewModels)
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