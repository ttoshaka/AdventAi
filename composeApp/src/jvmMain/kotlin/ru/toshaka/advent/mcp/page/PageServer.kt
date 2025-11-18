package ru.toshaka.advent.mcp.page

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.RegisteredTool
import kotlinx.serialization.json.*
import org.jsoup.Jsoup
import ru.toshaka.advent.mcp.BaseServer

class PageServer : BaseServer() {
    override val port: Int = 3003
    override val host: String = "0.0.0.0"
    override val name: String = "page_server"
    override val version: String = "0.0.0.0"

    override fun createTools(): List<RegisteredTool> {
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
            val response = buildString {
                callToolRequest.arguments["url"]!!.jsonArray.forEach {
                    val url = it.jsonPrimitive.content
                    println("Loading url... $url")
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