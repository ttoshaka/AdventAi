package ru.toshaka.advent.mcp.obsidian

import ru.toshaka.advent.mcp.BaseClient

class ObsidianClient : BaseClient() {
    override val name: String = "obsidian_client"
    override val version: String = "0.0.1"
    override val url: String = "http://localhost:3002"
}