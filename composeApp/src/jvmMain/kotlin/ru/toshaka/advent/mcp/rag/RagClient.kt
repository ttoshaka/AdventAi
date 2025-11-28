package ru.toshaka.advent.mcp.rag

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import ru.toshaka.advent.SearchRequest
import ru.toshaka.advent.SearchResponse
import ru.toshaka.advent.mcp.BaseClient

class RagClient : BaseClient() {
    override val name: String = "rag_client"
    override val version: String = "0.0.1"
    override val url: String = "http://localhost:3007"

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
            requestTimeoutMillis = 60_000
        }
    }

    override suspend fun call(name: String, args: String): String {
        val text = Json.parseToJsonElement(args).jsonObject["text"]!!.jsonPrimitive.content
        val rerank = Json.parseToJsonElement(args).jsonObject["rerank"]?.jsonPrimitive?.boolean ?: false
        val response = client.post("http://localhost:9000/search") {
            contentType(ContentType.Application.Json)
            setBody(
                SearchRequest(
                    query = text,
                    top_k = 20,
                    top_n = if (rerank) 60 else null
                )
            )
        }.body<SearchResponse>()
        return buildString {
            response.results.forEach {
                appendLine(it.text)
            }
        }
    }
}