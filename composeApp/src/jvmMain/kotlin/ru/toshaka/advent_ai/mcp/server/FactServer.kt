package ru.toshaka.advent_ai.mcp.server

import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.RegisteredTool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcp
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.putJsonObject
import ru.toshaka.advent_ai.network.api.DeepSeekApi
import ru.toshaka.advent_ai.network.model.ChatRequest
import ru.toshaka.advent_ai.network.model.Message

class FactServer {

    private val deepSeekApi = DeepSeekApi()

    suspend fun launch() {
        embeddedServer(CIO, port = 3002, host = "0.0.0.0") {
            mcp { return@mcp configureMPC() }
        }.startSuspend(wait = true)
    }

    private fun configureMPC(): Server {
        val server = Server(
            serverInfo = Implementation(
                name = "fact-server", version = "0.0.1"
            ),
            options = ServerOptions(
                capabilities = ServerCapabilities(
                    tools = ServerCapabilities.Tools(listChanged = true)
                )
            )
        )
        server.addTools(createTools())
        return server
    }

    private fun createTools(): List<RegisteredTool> {
        val factTool = RegisteredTool(
            Tool(
                name = "request-fact",
                description = "Пишет случайный интересный факт на заданную тему",
                inputSchema = Tool.Input(
                    properties = buildJsonObject {
                        putJsonObject("theme") {
                            put("type", JsonPrimitive("string"))
                            put("description", JsonPrimitive("theme, Тема на которую нужно написать факт"))
                        }
                    },
                    required = listOf("theme")
                )
            )
        ) { answer ->
            val theme = answer.arguments["theme"]?.jsonPrimitive?.content
            val response = deepSeekApi.chat(
                ChatRequest(
                    model = "deepseek-chat",
                    messages = listOf(
                        Message(
                            role = "system",
                            content = "Тебе нужно написать интересный факт на заданную тему. Только факт и больше ничего."
                        ),
                        Message(
                            role = "user",
                            content = theme!!
                        )
                    )
                )
            )
            val fact = response.choices.first().message.content
            CallToolResult(content = listOf(TextContent(fact)))
        }
        return listOf(factTool)
    }
}