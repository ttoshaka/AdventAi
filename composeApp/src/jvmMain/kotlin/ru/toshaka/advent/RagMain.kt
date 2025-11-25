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
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

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
        requestTimeoutMillis = 60_000
    }
}
private const val BASE_URL = "http://localhost:9000"

fun main() = runBlocking {
    val chunks = loadFb2AndChunk("C:\\Users\\Anton\\IdeaProjects\\AdventAi\\starik_i_more.fb2")
    add(chunks)
    //search("Как зовут мальчика?")//Манолин
    awaitCancellation()
    println()
}

fun loadFb2AndChunk(
    filePath: String,
    chunkSize: Int = 100,
    overlap: Int = 50,
): List<String> {
    val xmlFile = File(filePath)
    val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val doc = docBuilder.parse(xmlFile)
    doc.documentElement.normalize()

    val paragraphs = doc.getElementsByTagName("p")
    val textBuilder = StringBuilder()
    for (i in 0 until paragraphs.length) {
        val node = paragraphs.item(i)
        val paragraphText = node.textContent.trim()
        if (paragraphText.isNotEmpty()) {
            textBuilder.append(paragraphText).append(" ")
        }
    }

    val allWords = textBuilder.toString().split("\\s+".toRegex())
    val chunks = mutableListOf<String>()
    var start = 0
    while (start < allWords.size) {
        val end = (start + chunkSize).coerceAtMost(allWords.size)
        val chunk = allWords.subList(start, end).joinToString(" ")
        chunks.add(chunk)
        start += (chunkSize - overlap)
    }

    return chunks
}

suspend fun search(query: String): SearchResponse =
    client.post("$BASE_URL/search") {
        contentType(ContentType.Application.Json)
        setBody(
            SearchRequest(
                query = query,
                top_k = 5,
            )
        )
    }.body()

@Serializable
data class SearchRequest(
    val query: String,
    val top_k: Int
)

@Serializable
data class SearchResponse(
    val results: List<Result>,
) {
    @Serializable
    data class Result(
        val text: String,
        val distance: Float
    )
}

suspend fun add(chunks: List<String>): AddResponse =
    client.post("$BASE_URL/add_chunks") {
        contentType(ContentType.Application.Json)
        setBody(
            AddRequest(
                chunks = chunks,
            )
        )
    }.body()

@Serializable
data class AddRequest(
    val chunks: List<String>,
)

@Serializable
data class AddResponse(
    val status: String,
    val added_chunks: Int,
)