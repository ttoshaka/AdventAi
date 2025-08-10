package ru.toshaka.advent_ai

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import ru.toshaka.advent_ai.model.ChatRequest
import ru.toshaka.advent_ai.model.ChatResponse
import ru.toshaka.advent_ai.model.DisplayedMessage
import ru.toshaka.advent_ai.model.Message

class MainViewModel {

    private val viewModelScope = CoroutineScope(Dispatchers.Default)
    private val messageHistory = mutableListOf<Message>()
    private val client = HttpClient(CIO) {
        install(Logging)
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 60_000
        }
    }

    private val _messages = MutableStateFlow<List<DisplayedMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    init {
        messageHistory.add(Message("system", "You are a helpful assistant."))
    }

    fun onClick(message: String) {
        _messages.value = _messages.value + DisplayedMessage(message, DisplayedMessage.Author.USER)
        viewModelScope.launch {
            val response = chat(message)
            _messages.value = _messages.value + DisplayedMessage(response, DisplayedMessage.Author.AI)
        }
    }

    private suspend fun chat(prompt: String): String {
        messageHistory.add(Message("user", prompt))
        val request = ChatRequest(
            model = "deepseek-chat",
            messages = messageHistory.toList()
        )
        val response = client.post("https://api.deepseek.com/chat/completions") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $KEY")
            setBody(request)
        }.body<ChatResponse>()

        val assistantReply = response.choices.first().message.content

        messageHistory.add(Message("assistant", assistantReply))

        return assistantReply
    }

    companion object {
        private const val KEY = "API-KEY"
    }
}