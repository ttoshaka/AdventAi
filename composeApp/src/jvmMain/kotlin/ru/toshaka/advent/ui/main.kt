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
        MainViewModel(1, 0f, deepSeekApi, messageRepository),
        MainViewModel(2, 0.7f, deepSeekApi, messageRepository),
        MainViewModel(3, 1.2f, deepSeekApi, messageRepository),
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