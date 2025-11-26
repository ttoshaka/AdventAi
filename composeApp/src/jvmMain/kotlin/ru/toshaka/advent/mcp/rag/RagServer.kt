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
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.RegisteredTool
import kotlinx.serialization.json.*
import ru.toshaka.advent.SearchRequest
import ru.toshaka.advent.SearchResponse
import ru.toshaka.advent.mcp.BaseServer

class RagServer : BaseServer() {
    override val port: Int = 3007
    override val host: String = "0.0.0.0"
    override val name: String = "rag_server"
    override val version: String = "0.0.1"

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

    override fun createTools(): List<RegisteredTool> {
        val factTool = RegisteredTool(
            Tool(
                title = null,
                outputSchema = null,
                annotations = null,
                name = "rag",
                description = "Получить дополнитульную информацию из базы знаний(RAG) агента. В ответе будут чанки содержащие нужную информацию.",
                inputSchema = Tool.Input(
                    properties = buildJsonObject {
                        putJsonObject("text") {
                            put("type", JsonPrimitive("string"))
                            put(
                                "description",
                                JsonPrimitive("Запрос пользователя.")
                            )
                        }
                        putJsonObject("rerank") {
                            put("type", JsonPrimitive("boolean"))
                            put(
                                "description",
                                JsonPrimitive("Нужно ли использовать реранкинг")
                            )
                        }
                    },
                    required = listOf("text")
                )
            )
        ) { callToolRequest ->
            val text = callToolRequest.arguments["text"]!!.jsonPrimitive.content
            val rerank = callToolRequest.arguments["rerank"]?.jsonPrimitive?.boolean ?: false
            val response = client.post("http://localhost:9000/search") {
                contentType(ContentType.Application.Json)
                setBody(
                    SearchRequest(
                        query = text,
                        top_k = 5,
                        top_n = if (rerank) 20 else null
                    )
                )
            }.body<SearchResponse>()
            CallToolResult(
                content = listOf(
                    TextContent(
                        buildString {
                            response.results.forEach {
                                appendLine(it.text)
                            }
                        }
                    )))

        }
        return listOf(factTool)
    }
}