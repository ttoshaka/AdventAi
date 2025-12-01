package ru.toshaka.advent.mcp.github

import ru.toshaka.advent.mcp.BaseClient

class GithubClient : BaseClient() {
    override val name: String = "github_client"
    override val version: String = "0.0.1"
    override val url: String = "http://localhost:3008"
}