package ru.toshaka.advent_ai.mcp.client

import io.ktor.client.*
import io.ktor.client.plugins.sse.*
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.client.ClientOptions
import io.modelcontextprotocol.kotlin.sdk.client.SseClientTransport

class TelegramBotClient {
    private val client = Client(
        clientInfo = Implementation("chat-ui", "0.0.1"),
        options = ClientOptions()
    )
    private val httpClient = HttpClient {
        install(SSE)
    }
    private val transport = SseClientTransport(
        client = httpClient,
        urlString = "http://localhost:3001"
    )

    suspend fun connect(): List<Tool> {
        client.connect(transport)
        return client.listTools()?.tools.orEmpty()
    }

    suspend fun call(
        name: String,
        arguments: Map<String, Any?>,
    ): String {
        val response = client.callTool(
            name = name,
            arguments = arguments,
        )
        return (response?.content?.first() as TextContent).text!!
    }
}