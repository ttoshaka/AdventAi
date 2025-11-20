package ru.toshaka.advent.mcp.obsidian

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

class ObsidianServer : BaseServer() {
    override val port: Int = 3002
    override val host: String = "0.0.0.0"
    override val name: String = "obsidian_server"
    override val version: String = "0.0.1"

    override fun createTools(): List<RegisteredTool> {
        val readerTool = RegisteredTool(
            Tool(
                title = null,
                outputSchema = null,
                annotations = null,
                name = "Reader",
                description = "Считывает данные из указанной заметки",
                inputSchema = Tool.Input(
                    properties = buildJsonObject {
                        putJsonObject("fileName") {
                            put("type", JsonPrimitive("string"))
                            put("description", JsonPrimitive("Название заметки"))
                        }
                    },
                    required = listOf("fileName")
                )
            )
        ) { callToolRequest ->
            val first = callToolRequest.arguments["fileName"]!!.jsonPrimitive.content
            val file = File("C:\\Users\\Anton\\Documents\\Obsidian Vault\\$first.md")
            val text = file.readText()
            CallToolResult(content = listOf(TextContent(text)))
        }
        val writerTool = RegisteredTool(
            Tool(
                title = null,
                outputSchema = null,
                annotations = null,
                name = "Writer",
                description = "Используется для сохранение любой информации, которую пользователь явно просит сохранить или записать.",
                inputSchema = Tool.Input(
                    properties = buildJsonObject {
                        putJsonObject("fileName") {
                            put("type", JsonPrimitive("string"))
                            put("description", JsonPrimitive("Название файла"))
                        }
                        putJsonObject("content") {
                            put("type", JsonPrimitive("string"))
                            put("description", JsonPrimitive("Контент для записи в файл"))
                        }
                    },
                    required = listOf("fileName, content")
                )
            )
        ) { callToolRequest ->
            val fileName = callToolRequest.arguments["fileName"]!!.jsonPrimitive.content
            val content = callToolRequest.arguments["content"]!!.jsonPrimitive.content
            val file = File("C:\\Users\\Anton\\Documents\\AdventVault\\$fileName.md")
            val text = file.writeText(content)
            CallToolResult(content = listOf(TextContent("Файл с именем $fileName создана")))
        }
        return listOf(readerTool, writerTool)
    }
}