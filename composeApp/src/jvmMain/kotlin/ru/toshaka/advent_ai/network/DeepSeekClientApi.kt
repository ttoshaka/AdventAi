package ru.toshaka.advent_ai.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import ru.toshaka.advent_ai.network.model.ChatRequest
import ru.toshaka.advent_ai.network.model.ChatResponse
import ru.toshaka.advent_ai.network.model.Message

class DeepSeekClientApi(
    private val system: String,
) {

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

    init {
        messageHistory.add(Message("system", system))
    }

    suspend fun chat(prompt: String): String {
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