package ru.toshaka.advent_ai.network.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import ru.toshaka.advent_ai.network.model.ChatRequest
import ru.toshaka.advent_ai.network.model.ChatResponse

class DeepSeekApi {

    private val client = HttpClient(CIO) {
        install(Logging) {
            level = LogLevel.BODY
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = TIMEOUT
        }
        install(Auth) {
            bearer {
                loadTokens {
                    BearerTokens(KEY, null)
                }
            }
        }
    }

    suspend fun chat(request: ChatRequest): ChatResponse =
        client.post(URL) {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(request)
        }.body<ChatResponse>()

    companion object {
        const val KEY: String = "API-KEY"
        private const val URL: String = "https://api.deepseek.com/chat/completions"
        private const val TIMEOUT: Long = 60_000
    }
}