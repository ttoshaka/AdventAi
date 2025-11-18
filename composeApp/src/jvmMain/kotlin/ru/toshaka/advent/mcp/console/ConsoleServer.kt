package ru.toshaka.advent.mcp.console

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.RegisteredTool
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.putJsonObject
import ru.toshaka.advent.mcp.BaseServer

class ConsoleServer : BaseServer() {
    override val port: Int = 3004
    override val host: String = "0.0.0.0"
    override val name: String = "page_server"
    override val version: String = "0.0.1"

    override fun createTools(): List<RegisteredTool> {
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