package ru.toshaka.advent.mcp

import io.modelcontextprotocol.kotlin.sdk.Tool
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.toshaka.advent.data.model.ChatRequest
import ru.toshaka.advent.mcp.file.FileClient
import ru.toshaka.advent.mcp.file.FileServer
import ru.toshaka.advent.mcp.github.GithubClient
import ru.toshaka.advent.mcp.github.GithubServer
import ru.toshaka.advent.mcp.rag.RagClient
import ru.toshaka.advent.mcp.rag.RagServer

class McpManager {

    private val mcp = mapOf<BaseServer, BaseClient>(
        //RagServer() to RagClient(),
        //GithubServer() to GithubClient(),
        //FileServer() to FileClient()
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