package ru.toshaka.advent

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ru.toshaka.advent.ui.MainViewModel
import java.nio.charset.StandardCharsets

@Serializable
data class Question(
    val id: Int,
    val question: String,
)

fun main() = runBlocking {
    val viewModel = MainViewModel()
    delay(5000)
    val json = Thread.currentThread()
        .contextClassLoader
        .getResourceAsStream("questions.json")!!
        .readBytes()
        .toString(StandardCharsets.UTF_8)
    Json.decodeFromString<List<Question>>(json).forEach { viewModel.sendQuestion(it) }
    awaitCancellation()
    println()
}