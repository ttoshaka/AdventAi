package ru.toshaka.advent.mcp.code

import ru.toshaka.advent.mcp.BaseClient

class CodeClient : BaseClient() {
    override val name: String = "code_client"
    override val version: String = "0.0.1"
    override val url: String = "http://localhost:3005"
}