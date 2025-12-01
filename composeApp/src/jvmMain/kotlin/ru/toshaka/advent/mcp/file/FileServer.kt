package ru.toshaka.advent.mcp.file

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.RegisteredTool
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.putJsonObject
import ru.toshaka.advent.mcp.BaseServer
import java.io.File

class FileServer : BaseServer() {
    override val port: Int = 3009
    override val host: String = "0.0.0.0"
    override val name: String = "file_server"
    override val version: String = "0.0.1"

    override fun createTools(): List<RegisteredTool> {
        val createFileTool = RegisteredTool(
            Tool(
                title = null,
                outputSchema = null,
                annotations = null,
                name = "CreateFile",
                description = "Создаёт файл с указанным содержимым",
                inputSchema = Tool.Input(
                    properties = buildJsonObject {
                        putJsonObject("content") {
                            put("type", JsonPrimitive("string"))
                            put("description", JsonPrimitive("Текст, который будет записан в файл"))
                        }
                    },
                    required = listOf("content")
                ),
            )
        ) { callToolRequest ->
            val content = callToolRequest.arguments["content"]?.jsonPrimitive?.content ?: ""

            return@RegisteredTool try {
                val dir = File("docs")
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, "structure.md")
                file.writeText(content)
                CallToolResult(content = listOf(TextContent("Файл успешно создан: ${file.path}")))
            } catch (e: Exception) {
                CallToolResult(content = listOf(TextContent("Ошибка при создании файла: ${e.message}")))
            }
        }

        return listOf(createFileTool)
    }
}
