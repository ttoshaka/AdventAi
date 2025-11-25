package ru.toshaka.advent.mcp

import io.modelcontextprotocol.kotlin.sdk.Tool
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.toshaka.advent.data.model.ChatRequest
import ru.toshaka.advent.mcp.code.CodeClient
import ru.toshaka.advent.mcp.code.CodeServer
import ru.toshaka.advent.mcp.obsidian.ObsidianClient
import ru.toshaka.advent.mcp.obsidian.ObsidianServer
import ru.toshaka.advent.mcp.page.PageClient
import ru.toshaka.advent.mcp.page.PageServer
import ru.toshaka.advent.mcp.rag.RagClient
import ru.toshaka.advent.mcp.rag.RagServer
import ru.toshaka.advent.mcp.util.UtilsClient
import ru.toshaka.advent.mcp.util.UtilsServer

class McpManager {

    private val mcp = mapOf(
        RagServer() to RagClient(),
        PageServer() to PageClient(),
        UtilsServer() to UtilsClient(),
        ObsidianServer() to ObsidianClient(),
        CodeServer() to CodeClient(),
    )
    private val clients = mutableMapOf<String, BaseClient>()
    private val tools = mutableListOf<Tool>()

    suspend fun launchServer() = coroutineScope {
        mcp.forEach { (server, client) ->
            launch { server.launch() }
            launch {
                delay(1_000)
                val tools = client.connect()
                tools.forEach { tool ->
                    this@McpManager.tools.add(tool)
                    this@McpManager.clients[tool.name] = client
                }
            }
        }
    }

    suspend fun callTool(name: String, args: String): String {
        val response = clients[name]?.call(name, args)!!
        println("Tool $name with $args reponse $response")
        return response
    }

    fun getTools(): List<ChatRequest.Tool> {
        return tools.map { tool ->
            ChatRequest.Tool(
                type = "function",
                function = ChatRequest.ToolFunction(
                    name = tool.name,
                    description = tool.description,
                    parameters = ChatRequest.ToolParameter(
                        type = tool.inputSchema.type,
                        properties = tool.inputSchema.properties,
                        required = tool.inputSchema.required
                    )
                )
            )
        }
    }
}