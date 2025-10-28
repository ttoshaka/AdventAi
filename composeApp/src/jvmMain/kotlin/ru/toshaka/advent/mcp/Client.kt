package ru.toshaka.advent.mcp

import io.ktor.client.*
import io.ktor.client.plugins.sse.*
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.client.ClientOptions
import io.modelcontextprotocol.kotlin.sdk.client.SseClientTransport

class Client {

    private val client = Client(
        clientInfo = Implementation("test_mcp_client", "0.0.1"),
        options = ClientOptions()
    )
    private val httpClient = HttpClient {
        install(SSE)
    }
    private val transport = SseClientTransport(
        client = httpClient,
        urlString = "http://localhost:3002"
    )

    suspend fun connect(): List<Tool> {
        client.connect(transport)
        return client.listTools().tools
    }

    suspend fun call(f: Int, s: Int): String {
        val response = client.callTool(
            name = "summarizer",
            arguments = mapOf("first" to f, "second" to s)
        )
        return (response?.content?.first() as TextContent).text!!
    }
}