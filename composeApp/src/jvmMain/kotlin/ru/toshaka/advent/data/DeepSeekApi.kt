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

    suspend fun sendChat(messages: List<Pair<String, String>>): DeepSeekResponse {
        val requestBody = DeepSeekRequest(
            messages = buildList {
                add(
                    DeepSeekRequest.DeepSeekMessage(
                        content = "Ты AI-ассистент. Ты должен собрать следующую информацию о пользователе:\n" +
                                "1)Имя\n" +
                                "2)Дата рождения\n" +
                                "3)Город проживания\n" +
                                "4)Любимый язык программирования\n" +
                                "Если пользователь не указал какую либо информацию, ты должен попросить её указать. Один параметр за один раз.\n" +
                                "Когда все данные будут собраны, уведоми об этом пользователя и напишу информацию.\n" +
                                "Твой ответ должен соответствовать следующему формату:\n$JsonDescription\n" +
                                "Примеры:\n$example1\n$example2",
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