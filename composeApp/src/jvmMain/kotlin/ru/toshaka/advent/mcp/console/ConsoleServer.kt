package ru.toshaka.advent.mcp.console

import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.RegisteredTool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcp
import kotlinx.serialization.json.*

class ConsoleServer {
    suspend fun launch() {
        embeddedServer(CIO, port = 3004, host = "0.0.0.0") {
            mcp { return@mcp configureMCP() }
        }.start(true)
    }

    private fun configureMCP(): Server {
        val server = Server(
            serverInfo = Implementation(name = "command_server", version = "0.0.1"),
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
        val execTool = RegisteredTool(
            Tool(
                title = null,
                outputSchema = null,
                annotations = null,
                name = "ExecuteCommand",
                description = "Выполняет переданную консольную команду и возвращает её вывод",
                inputSchema = Tool.Input(
                    properties = buildJsonObject {
                        putJsonObject("command") {
                            put("type", JsonPrimitive("string"))
                            put("description", JsonPrimitive("Команда для выполнения. То что пропишется в консоли."))
                        }
                    },
                    required = listOf("command")
                ),
            )
        ) { callToolRequest ->
            val command = callToolRequest.arguments["command"]?.jsonPrimitive?.content ?: ""
            val process = ProcessBuilder(*command.split(" ").toTypedArray())
                .redirectErrorStream(true)
                .start()

            val output = process.inputStream.bufferedReader().use { it.readText() }
            process.waitFor()
            CallToolResult(content = listOf(TextContent(output)))
        }

        return listOf(execTool)
    }
}