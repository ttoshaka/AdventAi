package ru.toshaka.advent_ai

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.RegisteredTool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcp
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonObject
import ru.toshaka.advent_ai.network.api.DeepSeekApi.Companion.KEY

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
    server.addTools(listOf(modelsLis))
    return server
}

val httpClient = HttpClient {
    install(Logging) {
        level = LogLevel.ALL
    }
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        })
    }
    install(Auth) {
        bearer {
            loadTokens {
                BearerTokens(KEY, null)
            }
        }
    }
}

val modelsLis = RegisteredTool(
    Tool(
        name = "get-letters-count",
        description = "Выдает список доступных языковых моделей",
        inputSchema = Tool.Input()
    )
) { _ ->
    val response = httpClient.get(urlString = "https://api.deepseek.com/models") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
    }.body<String>()
    CallToolResult(content = listOf(TextContent(response)))
}

