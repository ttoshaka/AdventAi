package ru.toshaka.advent.mcp.code

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.RegisteredTool
import kotlinx.serialization.json.*
import ru.toshaka.advent.mcp.BaseServer
import java.io.File

class CodeServer : BaseServer() {
    override val port: Int = 3005
    override val host: String = "0.0.0.0"
    override val name: String = "code_server"
    override val version: String = "0.0.1"

    override fun createTools(): List<RegisteredTool> {
        val readerTool = RegisteredTool(
            Tool(
                title = null,
                outputSchema = null,
                annotations = null,
                name = "CodeReader",
                description = "Инструмент для получения кода проекта",
                inputSchema = Tool.Input(
                    properties = buildJsonObject {
                        putJsonObject("path") {
                            put("type", JsonPrimitive("string"))
                            put("description", JsonPrimitive("Путь к папке с проектом"))
                        }
                    },
                    required = listOf("path")
                )
            )
        ) { callToolRequest ->
            val fileName = callToolRequest.arguments["path"]!!.jsonPrimitive.content
            val code = getOptimizedProjectCode(fileName)
            CallToolResult(content = listOf(TextContent(code)))
        }
        return listOf(readerTool)
    }

    private fun getOptimizedProjectCode(projectPath: String, maxFileSizeKb: Int = 100): String {
        val root = File(projectPath)
        if (!root.exists() || !root.isDirectory) {
            throw IllegalArgumentException("Путь $projectPath не существует или не является директорией")
        }

        val ignoreDirs = listOf("build", ".idea", ".gradle", ".git", "out")
        val allowedExt = listOf("kt", "java", "xml", "gradle", "kts", "json", "yml", "yaml", "properties")

        val json = buildJsonObject {
            root.walkTopDown()
                .filter { file ->
                    file.isFile &&
                            file.extension in allowedExt &&
                            ignoreDirs.none { file.path.contains("/$it/") }
                }
                .forEach { file ->
                    val relativePath = file.relativeTo(root).path
                    val text = try {
                        if (file.length() > maxFileSizeKb * 1024)
                            "// Файл слишком большой (${file.length() / 1024}KB), пропущен."
                        else file.readText()
                    } catch (e: Exception) {
                        "// Ошибка чтения файла: ${e.message}"
                    }

                    put(relativePath, JsonPrimitive(text))
                }
        }

        return Json.encodeToString(JsonObject.serializer(), json)
    }
}