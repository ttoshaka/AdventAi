package ru.toshaka.advent.mcp.file

import ru.toshaka.advent.mcp.BaseClient

class FileClient : BaseClient() {
    override val name: String = "file_client"
    override val version: String = "0.0.1"
    override val url: String = "http://localhost:3009"
}