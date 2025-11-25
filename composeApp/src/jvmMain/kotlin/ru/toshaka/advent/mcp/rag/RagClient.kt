package ru.toshaka.advent.mcp.rag

import ru.toshaka.advent.mcp.BaseClient

class RagClient : BaseClient() {
    override val name: String = "rag_client"
    override val version: String = "0.0.1"
    override val url: String = "http://localhost:3007"
}