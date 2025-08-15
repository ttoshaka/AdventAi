package ru.toshaka.advent_ai

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

fun main() {
    embeddedServer(CIO, port = 3001, host = "0.0.0.0") {
        mcp { return@mcp configureMPC() }
    }.start(wait = true)
}

private fun configureMPC(): Server {
    val server = Server(
        serverInfo = Implementation(
            name = "news-mpc-server", version = "0.0.1"
        ),
        options = ServerOptions(
            capabilities = ServerCapabilities(
                tools = ServerCapabilities.Tools(listChanged = true)
            )
        )
    )
    server.addTools(listOf(countLetters))
    return server
}

val countLetters = RegisteredTool(
    Tool(
        name = "get-letters-count",
        description = "Подсчитывает количество символов в сообщение",
        inputSchema = Tool.Input(
            properties = buildJsonObject {
                putJsonObject("message") {
                    put("type", JsonPrimitive("string"))
                    put("description", JsonPrimitive("message, Message from user"))
                }
            },
            required = listOf("message")
        )
    )
) { request ->
    val category = request.arguments["message"]?.jsonPrimitive?.content ?: return@RegisteredTool CallToolResult(
        content = listOf(TextContent("Required field 'message' is missing"))
    )
    CallToolResult(content = listOf(TextContent("Количество символов ${category.count()}")))
}

