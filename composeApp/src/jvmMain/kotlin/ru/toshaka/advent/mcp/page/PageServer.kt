package ru.toshaka.advent.mcp.page

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.RegisteredTool
import kotlinx.serialization.json.*
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import ru.toshaka.advent.mcp.BaseServer

class PageServer : BaseServer() {
    override val port: Int = 3003
    override val host: String = "0.0.0.0"
    override val name: String = "page_server"
    override val version: String = "0.0.1"

    override fun createTools(): List<RegisteredTool> {
        val factTool = RegisteredTool(
            Tool(
                title = null,
                outputSchema = null,
                annotations = null,
                name = "Page",
                description = "Загрузка и парсинг содержимого переданных web-сайтов и RSS-лент, веб-страниц и всего остального что имеет url. Этот инструмент умеет парсить RSS-ленты",
                inputSchema = Tool.Input(
                    properties = buildJsonObject {
                        putJsonObject("url") {
                            put("type", JsonPrimitive("array"))
                            putJsonObject("items") {
                                put("type", JsonPrimitive("string"))
                            }
                            put(
                                "description",
                                JsonPrimitive("Список url web-сайтов, содержимое которых нужно загрузить и распарсить.")
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
                    val content = loadUrl(url)
                    appendLine("$url - $content")
                    appendLine()
                }
            }
            println("URL response = $response")
            CallToolResult(content = listOf(TextContent(response)))
        }
        return listOf(factTool)
    }

    private fun loadUrl(url: String): String {
        val body = Jsoup.connect(url).ignoreContentType(true).timeout(60_000).execute().body()
        return if (body.trimStart().startsWith("<rss") || body.contains("<channel>")) {
            loadRssContent(body)
        } else {
            loadHtmlContent(body)
        }
    }

    private fun loadRssContent(xml: String): String {
        val doc = Jsoup.parse(xml, "", Parser.xmlParser())
        val items = doc.select("item")
        return buildString {
            items.forEach { item ->
                val title = item.selectFirst("title")?.text().orEmpty()
                val link = item.selectFirst("link")?.text().orEmpty()
                val descriptionRaw = item.selectFirst("description")?.text().orEmpty()
                val description = Jsoup.parse(descriptionRaw).text()
                appendLine("Title: $title")
                appendLine("Link: $link")
                appendLine("Description: $description")
                appendLine()
            }
        }
    }

    private fun loadHtmlContent(html: String): String {
        val doc = Jsoup.parse(html)
        doc.select("script, style, header, footer, nav, noscript, iframe").remove()
        val main = doc.select("main, article, #content, .content").firstOrNull() ?: doc.body()
        return main.text().replace(Regex("\\s+"), " ").trim()
    }
}