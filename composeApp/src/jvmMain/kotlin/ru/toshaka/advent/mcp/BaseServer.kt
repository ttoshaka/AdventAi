package ru.toshaka.advent.mcp

import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.server.RegisteredTool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcp

abstract class BaseServer {

    abstract val port: Int
    abstract val host: String
    abstract val name: String
    abstract val version: String

    suspend fun launch() {
        embeddedServer(CIO, port = port, host = host) {
            mcp { return@mcp configureMCP() }
        }.startSuspend(true)
    }

    protected abstract fun createTools(): List<RegisteredTool>

    private fun configureMCP(): Server {
        val server = Server(
            serverInfo = Implementation(
                name = name, version = version
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
}