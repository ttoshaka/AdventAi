package ru.toshaka.advent

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import ru.toshaka.advent.mcp.github.GithubServer
import java.io.File
import java.security.MessageDigest

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

fun main() = runBlocking {
    val projectPath = "C:\\Users\\Anton\\IdeaProjects\\AdventAi"
    val chunks = buildRagChunks(projectPath)
    println("Total files: ${chunks.map { it.filePath }.distinct().size}")
    println("Total chunks: ${chunks.size}")
    val embeddings = chunks.map { it.toEmbeddingText() }
    add(embeddings)
    awaitCancellation()
    println()
}

suspend fun add(chunks: List<String>): GithubServer.AddResponse =
    client.post("http://localhost:9003/add_chunks") {
        contentType(ContentType.Application.Json)
        setBody(GithubServer.AddRequest(chunks))
    }.body()

data class CodeMetadata(
    val filePath: String,
    val fileName: String,
    val extension: String,
    val relativePath: String,
    val sizeBytes: Long,
    val sha256: String,
    val imports: List<String>,
    val packageName: String?
)

data class CodeChunk(
    val filePath: String,
    val content: String,
    val startLine: Int,
    val endLine: Int,
    val metadata: CodeMetadata
)

fun sha256(bytes: ByteArray): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
    return digest.joinToString("") { "%02x".format(it) }
}

fun extractPackage(lines: List<String>): String? {
    return lines.firstOrNull { it.trim().startsWith("package ") }
        ?.removePrefix("package ")
        ?.trim()
}

fun extractImports(lines: List<String>): List<String> {
    return lines.filter { it.trim().startsWith("import ") }
        .map { it.removePrefix("import ").trim() }
}

fun buildMeta(file: File, root: File): CodeMetadata {
    val lines = file.readLines()
    return CodeMetadata(
        filePath = file.absolutePath,
        fileName = file.name,
        extension = file.extension,
        relativePath = root.toPath().relativize(file.toPath()).toString(),
        sizeBytes = file.length(),
        sha256 = sha256(file.readBytes()),
        imports = extractImports(lines),
        packageName = extractPackage(lines)
    )
}

fun loadProjectFiles(root: File): List<File> {
    return root.walkTopDown()
        .filter { it.isFile }
        .filter { file ->
            val name = file.name.lowercase()
            name.endsWith(".kt") ||
                    name.endsWith(".kts") ||
                    name.endsWith(".java") ||
                    name.endsWith(".md") ||
                    name.endsWith(".gradle") ||
                    name.endsWith(".xml") ||
                    name.endsWith(".yaml") ||
                    name.endsWith(".yml")
        }
        .toList()
}

fun chunkFile(
    file: File,
    projectRoot: File,
    maxLines: Int = 200,
    overlap: Int = 20
): List<CodeChunk> {
    val lines = file.readLines()
    if (lines.isEmpty()) return emptyList()

    val meta = buildMeta(file, projectRoot)
    val chunks = mutableListOf<CodeChunk>()
    var start = 0

    while (start < lines.size) {
        val end = (start + maxLines).coerceAtMost(lines.size)
        val slice = lines.subList(start, end).joinToString("\n")

        chunks += CodeChunk(
            filePath = file.absolutePath,
            content = slice,
            startLine = start + 1,
            endLine = end,
            metadata = meta
        )

        start += (maxLines - overlap)
    }

    return chunks
}

fun buildRagChunks(rootPath: String): List<CodeChunk> {
    val root = File(rootPath)
    val allFiles = loadProjectFiles(root)

    val chunks = mutableListOf<CodeChunk>()
    for (file in allFiles) {
        chunks += chunkFile(file, root)
    }

    return chunks
}

fun CodeChunk.toEmbeddingText(): String {
    val meta = metadata

    val importsJoined = if (meta.imports.isNotEmpty())
        meta.imports.joinToString(", ")
    else
        ""

    return buildString {
        appendLine("[FILE_PATH]: ${meta.filePath}")
        appendLine("[RELATIVE_PATH]: ${meta.relativePath}")
        appendLine("[PACKAGE]: ${meta.packageName ?: ""}")
        appendLine("[IMPORTS]: $importsJoined")
        appendLine("[LINES]: ${startLine}-${endLine}")
        appendLine()
        appendLine("[CONTENT]")
        appendLine(content)
    }
}