package ru.toshaka.advent.mcp

import io.ktor.client.*
import io.ktor.client.plugins.sse.*
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.client.ClientOptions
import io.modelcontextprotocol.kotlin.sdk.client.SseClientTransport
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

abstract class BaseClient {

    abstract val name: String

    abstract val version: String

    abstract val url: String

    private val client by lazy {
        Client(
            clientInfo = Implementation(name, version),
            options = ClientOptions()
        )
    }
    private val httpClient by lazy {
        HttpClient {
            install(SSE)
        }
    }
    private val transport by lazy {
        SseClientTransport(
            client = httpClient,
            urlString = url
        )
    }

    suspend fun connect(): List<Tool> {
        client.connect(transport)
        return client.listTools().tools
    }

    open suspend fun call(name: String, args: String): String {
        val response = client.callTool(name, Json.parseToJsonElement(args).jsonObject)
        return (response?.content?.first() as TextContent).text!!
    }
}