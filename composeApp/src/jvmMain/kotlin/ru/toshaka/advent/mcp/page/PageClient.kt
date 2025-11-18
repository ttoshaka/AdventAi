package ru.toshaka.advent.mcp.page

import ru.toshaka.advent.mcp.BaseClient

class PageClient : BaseClient() {
    override val name: String = "page_client"
    override val version: String = "0.0.1"
    override val url: String = "http://localhost:3003"
}