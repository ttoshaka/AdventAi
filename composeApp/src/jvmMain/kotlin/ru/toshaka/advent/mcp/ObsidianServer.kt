package ru.toshaka.advent.mcp

import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.RegisteredTool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcp
import kotlinx.serialization.json.*
import java.io.File

class ObsidianServer {
    suspend fun launch() {
        embeddedServer(CIO, port = 3002, host = "0.0.0.0") {
            mcp { return@mcp configureMCP() }
        }.start(true)
    }

    private fun configureMCP(): Server {
        val server = Server(
            serverInfo = Implementation(
                name = "obsidian_server", version = "0.0.1"
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
        return listOf(factTool)
    }
}