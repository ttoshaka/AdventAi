package ru.toshaka.advent.data.model

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

class DeepSeekApi {
    private val client = HttpClient(CIO) {
        install(Logging) {
            level = LogLevel.BODY
        }
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                }
            )
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

    suspend fun sendChat(message: String): DeepSeekResponse {
        val requestBody = DeepSeekRequest(
            messages = listOf(
                DeepSeekRequest.DeepSeekMessage(
                    content = "Ты AI-шутник, который придумывает шутки в формате вопрос-ответ" +
                            "Отвечай в следующем json-формате:" +
                            "{\n" +
                            "  \"question\": \"Вопрос\",\n" +
                            "  \"answer\": \"Ответ\"\n" +
                            "}",
                    role = "system"
                ),
                DeepSeekRequest.DeepSeekMessage(
                    content = message,
                    role = "user"
                )
            ),
            model = "deepseek-chat",
            responseFormat = DeepSeekRequest.ResponseFormat("json_object"),
        )

        return client.post(URL) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.body()
    }

    companion object {
        const val KEY: String = "KEY"
        private const val URL: String = "https://api.deepseek.com/chat/completions"
        private const val TIMEOUT: Long = 60_000
    }
}