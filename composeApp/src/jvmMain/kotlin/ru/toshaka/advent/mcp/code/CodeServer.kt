package ru.toshaka.advent.mcp.code

import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.RegisteredTool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcp
import kotlinx.serialization.json.*
import java.io.File

class CodeServer {
    suspend fun launch() {
        embeddedServer(CIO, port = 3005, host = "0.0.0.0") {
            mcp { return@mcp configureMCP() }
        }.start(true)
    }

    private fun configureMCP(): Server {
        val server = Server(
            serverInfo = Implementation(
                name = "code_server", version = "0.0.1"
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