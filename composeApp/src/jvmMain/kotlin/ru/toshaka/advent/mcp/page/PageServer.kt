package ru.toshaka.advent.mcp.page

import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.RegisteredTool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcp
import kotlinx.serialization.json.*
import org.jsoup.Jsoup

class PageServer {
    suspend fun launch() {
        embeddedServer(CIO, port = 3003, host = "0.0.0.0") {
            mcp { return@mcp configureMCP() }
        }.start(true)
    }

    private fun configureMCP(): Server {
        val server = Server(
            serverInfo = Implementation(
                name = "page_server", version = "0.0.1"
            ),
            options = ServerOptions(
                capabilities = ServerCapabilities(
                    tools = ServerCapabilities.Tools(listChanged = true)
                )
            )
        )
        server.addTools(createTools())
        return server
    }

    private fun createTools(): List<RegisteredTool> {
        val factTool = RegisteredTool(
            Tool(
                title = null,
                outputSchema = null,
                annotations = null,
                name = "Page",
                description = "Загружает содержимое страницы сайта",
                inputSchema = Tool.Input(
                    properties = buildJsonObject {
                        putJsonObject("url") {
                            put("type", JsonPrimitive("array"))
                            putJsonObject("items") {
                                put("type", JsonPrimitive("string"))
                            }
                            put(
                                "description",
                                JsonPrimitive("Список url страниц сайтов, содержимое которых нужно загрузить.")
                            )
                        }
                    },
                    required = listOf("url")
                )
            )
        ) { callToolRequest ->
            println("QWE" + callToolRequest.arguments["url"])
            val url = callToolRequest.arguments["url"]!!.jsonArray
            val response = buildString {
                url.forEach {
                    val url = it.jsonPrimitive.content
                    val doc = Jsoup.connect(url).get()
                    doc.select("script, style, header, footer, nav, noscript, iframe").remove()
                    val main = doc.select("main, article, #content, .content").firstOrNull() ?: doc.body()
                    val text = main.text().replace(Regex("\\s+"), " ").trim()
                    appendLine("$url - $text")
                    appendLine()
                }
            }
            CallToolResult(content = listOf(TextContent(response)))
        }
        return listOf(factTool)
    }
}