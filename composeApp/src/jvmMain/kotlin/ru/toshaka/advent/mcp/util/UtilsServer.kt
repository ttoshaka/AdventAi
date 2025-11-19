package ru.toshaka.advent.mcp.util

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.RegisteredTool
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.putJsonObject
import ru.toshaka.advent.mcp.BaseServer
import java.text.SimpleDateFormat
import java.util.*

class UtilsServer : BaseServer() {

    override val port: Int = 3006
    override val host: String = "0.0.0.0"
    override val name: String = "utils_server"
    override val version: String = "0.0.1"

    override fun createTools(): List<RegisteredTool> {
        val timeTool = RegisteredTool(
            Tool(
                title = null,
                outputSchema = null,
                annotations = null,
                name = "time",
                description = "Возвращает текущую дату и время",
                inputSchema = Tool.Input(
                    properties = buildJsonObject {
                        putJsonObject("pattern") {
                            put("type", JsonPrimitive("string"))
                            put("description", JsonPrimitive("Паттерн для форматирования даты и времени"))
                        }
                    },
                    required = emptyList()
                )
            )
        ) { callToolRequest ->
            val pattern = callToolRequest.arguments["pattern"]?.jsonPrimitive?.content
            val time = SimpleDateFormat(pattern ?: "yyyy-MM-dd HH:mm:ss").format(Date())
            CallToolResult(content = listOf(TextContent(time)))
        }
        return listOf(timeTool)
    }
}