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
        val saveFileContent = RegisteredTool(
            Tool(
                title = null,
                outputSchema = null,
                annotations = null,
                name = "saveFileContent",
                description = "Сохраняет переданное содержимое в файл с указанным именем",
                inputSchema = Tool.Input(
                    properties = buildJsonObject {
                        putJsonObject("content") {
                            put("type", JsonPrimitive("string"))
                            put("description", JsonPrimitive("Содержимое для сохранения в файл"))
                        }
                        putJsonObject("path") {
                            put("type", JsonPrimitive("string"))
                            put("description", JsonPrimitive("Путь к файлу в который нужно сохранить содержимое"))
                        }
                    },
                    required = listOf("content", "path")
                ),
            )
        ) { callToolRequest ->
            val content = callToolRequest.arguments["content"]?.jsonPrimitive?.content ?: ""
            val path = callToolRequest.arguments["path"]?.jsonPrimitive?.content ?: ""
            return@RegisteredTool try {
                val file = File(path)
                file.writeText(content)
                CallToolResult(content = listOf(TextContent("Файл успешно создан: ${file.path}")))
            } catch (e: Exception) {
                CallToolResult(content = listOf(TextContent("Ошибка при создании файла: ${e.message}")))
            }
        }

        val getFileContent = RegisteredTool(
            Tool(
                title = null,
                outputSchema = null,
                annotations = null,
                name = "getFileContent",
                description = "Получить содержимое файла",
                inputSchema = Tool.Input(
                    properties = buildJsonObject {
                        putJsonObject("path") {
                            put("type", JsonPrimitive("string"))
                            put("description", JsonPrimitive("Путь к файлу содержимое которого нужно получить"))
                        }
                    },
                    required = listOf("path")
                ),
            )
        ) { callToolRequest ->
            val path = callToolRequest.arguments["path"]?.jsonPrimitive?.content ?: ""
            return@RegisteredTool try {
                val file = File(path)
                val content = file.readText()
                CallToolResult(content = listOf(TextContent("Содержимое файла ${file.path}:\n$content")))
            } catch (e: Exception) {
                CallToolResult(content = listOf(TextContent("Ошибка при чтение файла: ${e.message}")))
            }
        }

        return listOf(saveFileContent, getFileContent)
    }
}
