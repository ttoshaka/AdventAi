package ru.toshaka.advent.mcp.console

import ru.toshaka.advent.mcp.BaseClient

class ConsoleClient : BaseClient() {
    override val name: String = "command_client"
    override val version: String = "0.0.1"
    override val url: String = "http://localhost:3004"
}