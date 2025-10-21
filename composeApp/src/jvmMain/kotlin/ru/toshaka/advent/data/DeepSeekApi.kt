package ru.toshaka.advent.data

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
import ru.toshaka.advent.data.model.*

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

    suspend fun sendChat(
        messages: List<Pair<String, String>>,
        temperature: Float = 1f,
    ): DeepSeekResponse {
        println(JsonDescription)
        val requestBody = DeepSeekRequest(
            messages = buildList {
                add(
                    DeepSeekRequest.DeepSeekMessage(
                        content = """
                            Ты AI-ассистент. 
                                Только один тип сообщения за раз.
                                Ответ всегда должен содержать текст. Если у тебя нет вопроса или текста, то напиши почему тебе нечего ответить.
                                Примеры:
                                $example1
                                $example2
                                """,
                        role = "system"
                    )
                )
                addAll(
                    messages.map {
                        DeepSeekRequest.DeepSeekMessage(
                            content = it.first,
                            role = it.second,
                        )
                    }
                )

            },
            model = "deepseek-chat",
            responseFormat = DeepSeekRequest.ResponseFormat("json_object"),
            temperature = temperature,
        )

        return client.post(URL) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.body()
    }

    companion object {
        const val KEY: String = "sk-c388a56e101b4d989e16d87c6c080658"
        private const val URL: String = "https://api.deepseek.com/chat/completions"
        private const val TIMEOUT: Long = 60_000
    }
}