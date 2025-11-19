package ru.toshaka.advent.mcp.util

import ru.toshaka.advent.mcp.BaseClient

class UtilsClient: BaseClient() {
    override val name: String = "utils_client"
    override val version: String = "0.0.1"
    override val url: String = "http://localhost:3006"
}