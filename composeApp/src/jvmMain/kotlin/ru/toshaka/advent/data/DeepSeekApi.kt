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
import ru.toshaka.advent.data.model.DeepSeekRequest
import ru.toshaka.advent.data.model.DeepSeekResponse

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
        model: String,
        temperature: Float = 1f,
    ): DeepSeekResponse {
        val requestBody = DeepSeekRequest(
            messages = buildList {
                add(
                    DeepSeekRequest.DeepSeekMessage(
                        content = """
                            Reasoning: low
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
            model = model,
            responseFormat = DeepSeekRequest.ResponseFormat("text"),
            temperature = temperature,
        )

        return client.post(URL) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.body()
    }

    companion object {
        const val KEY: String = "KEY"
        private const val URL: String = "https://router.huggingface.co/v1/chat/completions"
        private const val TIMEOUT: Long = 60_000
    }
}