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
import ru.toshaka.advent.data.db.message.type
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

    suspend fun send(): ChatResponse {
        val requestBody = ChatRequest(
            messages = buildList {
                add(
                    ChatRequest.ChatMessage(
                        content = agentConfig.systemPrompt,
                        role = "system"
                    )
                )
                agentConfig.history().forEach { message ->
                    if (!message.history) return@forEach
                    add(
                        ChatRequest.ChatMessage(
                            content = message.content,
                            role = message.type
                        )
                    )
                }
            },
            model = agentConfig.model,
            responseFormat = ChatRequest.ResponseFormat("json_object"),
            temperature = agentConfig.temperature,
            maxTokens = agentConfig.maxTokens,
            tools = agentConfig.tools,
        )

        return client.post(agentConfig.baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.body()
    }

    suspend fun send(message: String): ChatResponse {
        val requestBody = ChatRequest(
            messages = buildList {
                add(
                    ChatRequest.ChatMessage(
                        content = agentConfig.systemPrompt,
                        role = "system"
                    )
                )
                add(
                    ChatRequest.ChatMessage(
                        content = message,
                        role = "user"
                    )
                )
            },
            model = agentConfig.model,
            responseFormat = ChatRequest.ResponseFormat("json_object"),
            temperature = agentConfig.temperature,
            maxTokens = agentConfig.maxTokens,
        )

        return client.post(agentConfig.baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.body()
    }

    suspend fun send(toolResponse: String, toolCall: ChatResponse.ToolCall): ChatResponse {
        val requestBody = ChatRequest(
            messages = buildList {
                add(
                    ChatRequest.ChatMessage(
                        content = agentConfig.systemPrompt,
                        role = "system"
                    )
                )
                agentConfig.history().forEach {
                    add(
                        ChatRequest.ChatMessage(
                            content = it.content,
                            role = it.type
                        )
                    )
                }
                add(
                    ChatRequest.ChatMessage(
                        content = null,
                        role = "assistant",
                        toolCalls = listOf(toolCall)
                    )
                )
                add(
                    ChatRequest.ChatMessage(
                        content = toolResponse,
                        role = "tool",
                        toolCallId = toolCall.id
                    )
                )
            },
            model = agentConfig.model,
            responseFormat = ChatRequest.ResponseFormat("json_object"),
            temperature = agentConfig.temperature,
            maxTokens = agentConfig.maxTokens,
            tools = agentConfig.tools,
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