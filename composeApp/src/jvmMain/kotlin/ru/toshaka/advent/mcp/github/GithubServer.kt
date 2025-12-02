package ru.toshaka.advent.mcp.github

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.RegisteredTool
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import org.kohsuke.github.GHIssueState
import org.kohsuke.github.GitHubBuilder
import ru.toshaka.advent.mcp.BaseServer
import java.io.File
import java.net.URL
import java.util.zip.ZipInputStream

class GithubServer : BaseServer() {
    override val port: Int = 3008
    override val host: String = "0.0.0.0"
    override val name: String = "github_server"
    override val version: String = "0.0.1"

    private val BASE_URL = "http://localhost:9002"
    private val client = HttpClient(CIO) {
        install(Logging) {
            level = LogLevel.BODY
        }
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                }
            )
        }
        install(HttpTimeout) {
            requestTimeoutMillis = Long.MAX_VALUE
        }
    }

    override fun createTools(): List<RegisteredTool> {
        val execTool = RegisteredTool(
            Tool(
                title = null,
                outputSchema = null,
                annotations = null,
                name = "read",
                description = "Скачивает проект с github, используя название переданной втеки и сохраняет его в базу знаний.",
                inputSchema = Tool.Input(
                    properties = buildJsonObject {
                        putJsonObject("branchName") {
                            put("type", JsonPrimitive("string"))
                            put("description", JsonPrimitive("Название ветки"))
                        }
                    },
                    required = listOf("branchName")
                ),
            )
        ) { callToolRequest ->
            val branchName = callToolRequest.arguments["branchName"]?.jsonPrimitive?.content ?: ""
            val chunks = loadRepoChunks(
                owner = "ttoshaka",
                repo = "AdventAi",
                branch = branchName,
                workDir = File("temp")
            )
            add(chunks)
            CallToolResult(content = listOf(TextContent("Данные сохранены в базе знаний RAG")))
        }

        val pullRequestTool = RegisteredTool(
            Tool(
                title = null,
                outputSchema = null,
                annotations = null,
                name = "pullRequest",
                description = "Возвращает изменения в пул реквесте",
                inputSchema = Tool.Input(
                    properties = buildJsonObject {
                        putJsonObject("repoName") {
                            put("type", JsonPrimitive("string"))
                            put(
                                "description",
                                JsonPrimitive("Название репозитория из которого нужно получть пул реквест")
                            )
                        }
                    },
                    required = listOf("repoName")
                ),
            )
        ) { callToolRequest ->
            val repoName = callToolRequest.arguments["repoName"]?.jsonPrimitive?.content ?: ""
            val github = GitHubBuilder().withOAuthToken("TOKEN").build()
            val pr = github.getRepository(repoName).searchPullRequests().list().toList().first { it.state == GHIssueState.OPEN }
           // delay(10_000L)
           // val diff = pr.pullRequest.diffUrl
            //TODO
            val content = URL("https://patch-diff.githubusercontent.com/raw/ttoshaka/AdventAi/pull/2.diff").readText()
            CallToolResult(content = listOf(TextContent("Изменения в пулл реквесте:\n$content")))
        }

        val commentTool = RegisteredTool(
            Tool(
                title = null,
                outputSchema = null,
                annotations = null,
                name = "comment",
                description = "Оставляет комментарий к пулл реквесту переданного репозитория",
                inputSchema = Tool.Input(
                    properties = buildJsonObject {
                        putJsonObject("repoName") {
                            put("type", JsonPrimitive("string"))
                            put(
                                "description",
                                JsonPrimitive("Название репозитория к пулл реквесту которого нужно оставить комментарий")
                            )
                        }
                        putJsonObject("text") {
                            put("type", JsonPrimitive("string"))
                            put(
                                "description",
                                JsonPrimitive("Содержимое коментария")
                            )
                        }
                    },
                    required = listOf("text")
                ),
            )
        ) { callToolRequest ->
            val repoName = callToolRequest.arguments["repoName"]?.jsonPrimitive?.content ?: ""
            val text = callToolRequest.arguments["text"]?.jsonPrimitive?.content ?: ""
            val github = GitHubBuilder().withOAuthToken("TOKEN").build()
            github.getRepository(repoName).searchPullRequests().list().toList().first { it.state == GHIssueState.OPEN }.comment(text)
            CallToolResult(content = listOf(TextContent("Комментарий отправлен")))
        }
        return listOf(execTool, pullRequestTool, commentTool)
    }

    private suspend fun add(chunks: List<String>): AddResponse =
        client.post("$BASE_URL/add_chunks") {
            contentType(ContentType.Application.Json)
            setBody(
                AddRequest(
                    chunks = chunks,
                )
            )
        }.body()

    private suspend fun loadRepoChunks(
        owner: String,
        repo: String,
        branch: String,
        workDir: File
    ): List<String> {
        if (!workDir.exists()) workDir.mkdirs()          // важно

        val zipFile = File(workDir, "repo.zip")
        val repoDir = File(workDir, "repo")

        downloadBranchZip(owner, repo, branch, zipFile)
        unzip(zipFile, repoDir)

        val files = collectSourceFiles(repoDir)
        val chunks = mutableListOf<String>()

        for (file in files) {
            val text = file.readText()
            chunks += chunkText(text, 1000)
        }

        return chunks
    }

    private suspend fun downloadBranchZip(
        owner: String,
        repo: String,
        branch: String,
        output: File
    ) {
        val client = HttpClient(CIO)
        val url = "https://github.com/$owner/$repo/archive/refs/heads/$branch.zip"
        val bytes = client.get(url).readBytes()
        output.writeBytes(bytes)
        client.close()
    }

    private fun unzip(zipFile: File, destDir: File) {
        destDir.mkdirs()
        ZipInputStream(zipFile.inputStream()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val outFile = File(destDir, entry.name)
                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile.mkdirs()
                    outFile.outputStream().use { zip.copyTo(it) }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
    }

    private fun collectSourceFiles(
        root: File,
        extensions: Set<String> = setOf("kt", "java", "ktx", "txt", "md", "kts", "py")
    ): List<File> {
        return root.walk()
            .filter { it.isFile && it.extension.lowercase() in extensions }
            .toList()
    }

    private fun chunkText(text: String, maxSize: Int = 2000): List<String> {
        val chunks = mutableListOf<String>()
        var start = 0
        while (start < text.length) {
            val end = minOf(start + maxSize, text.length)
            chunks.add(text.substring(start, end))
            start = end
        }
        return chunks
    }

    @Serializable
    data class SearchRequest(
        val query: String,
        val top_k: Int,
        val top_n: Int?,
    )

    @Serializable
    data class SearchResponse(
        val results: List<Result>,
    ) {
        @Serializable
        data class Result(
            val text: String,
        )
    }

    @Serializable
    data class AddRequest(
        val chunks: List<String>,
    )

    @Serializable
    data class AddResponse(
        val status: String,
        val added_chunks: Int,
    )
}