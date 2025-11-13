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
import ru.toshaka.advent.data.agent.AgentConfig
import ru.toshaka.advent.data.db.message.MessageEntity
import ru.toshaka.advent.data.model.ChatRequest
import ru.toshaka.advent.data.model.ChatResponse

class AgentApi(
    private val agentConfig: AgentConfig<*>
) {

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
                    BearerTokens(agentConfig.key, null)
                }
            }
        }
    }

    suspend fun send(system: String, maxTokens: Int, dialog: List<MessageEntity>): ChatResponse {
        val requestBody = ChatRequest(
            messages = buildList {
                add(
                    ChatRequest.ChatMessage(
                        content = system,
                        role = "system"
                    )
                )
                dialog.forEach {
                    add(
                        ChatRequest.ChatMessage(
                            content = it.content,
                            role = if (it.owner == 0L) "user" else "assistant"
                        )
                    )
                }
            },
            model = agentConfig.model,
            responseFormat = ChatRequest.ResponseFormat("text"),
            temperature = agentConfig.temperature,
            maxTokens = maxTokens,
        )

        return client.post(agentConfig.baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.body()
    }

    suspend operator fun invoke(
        message: String,
        history: List<Pair<String, String>>,
        force: Boolean = false
    ): ChatResponse {
        val requestBody = ChatRequest(
            messages = buildList {
                add(
                    ChatRequest.ChatMessage(
                        content = agentConfig.systemPrompt,
                        role = "system"
                    )
                )
                history.forEach {
                    add(
                        ChatRequest.ChatMessage(
                            content = it.second,
                            role = it.first
                        )
                    )
                }
                if (history.isEmpty() || force) {
                    add(
                        ChatRequest.ChatMessage(
                            content = message,
                            role = "user"
                        )
                    )
                }
            },
            model = agentConfig.model,
            responseFormat = ChatRequest.ResponseFormat("text"),
            temperature = agentConfig.temperature,
        )

        return client.post(agentConfig.baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.body()
    }

    companion object {
        private const val TIMEOUT: Long = 60_000
    }
}