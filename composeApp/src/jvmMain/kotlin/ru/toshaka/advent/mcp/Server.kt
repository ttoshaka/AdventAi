package ru.toshaka.advent.mcp

import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.RegisteredTool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcp
import kotlinx.serialization.json.*

class Server {

    suspend fun launch() {
        embeddedServer(CIO, port = 3002, host = "0.0.0.0") {
            mcp { return@mcp configureMCP() }
        }.start(true)
    }

    private fun configureMCP(): Server {
        val server = Server(
            serverInfo = Implementation(
                name = "test_name", version = "0.0.1"
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
                title = null,
                outputSchema = null,
                annotations = null,
                name = "summarizer",
                description = "Складывает два числа",
                inputSchema = Tool.Input(
                    properties = buildJsonObject {
                        putJsonObject("first") {
                            put("type", JsonPrimitive("string"))
                            put("description", JsonPrimitive("Первое число"))
                        }
                        putJsonObject("second") {
                            put("type", JsonPrimitive("string"))
                            put("description", JsonPrimitive("Второе число"))
                        }
                    },
                    required = listOf("first", "second")
                )
            )
        ) { callToolRequest ->
            println("callToolRequest = $callToolRequest")
            val first = callToolRequest.arguments["first"]?.jsonPrimitive?.int
            val second = callToolRequest.arguments["second"]?.jsonPrimitive?.int
            val sum = first!! + second!!
            CallToolResult(content = listOf(TextContent(sum.toString())))
        }
        return listOf(factTool)
    }
}