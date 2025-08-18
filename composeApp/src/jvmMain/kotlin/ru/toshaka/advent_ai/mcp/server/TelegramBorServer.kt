package ru.toshaka.advent_ai.mcp.server

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.RegisteredTool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcp
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class TelegramMessage(
    val text: String,
    val chatId: Long,
)

class TelegramBorServer {

    private var lastMessage: TelegramMessage? = null
    private val bot = bot {
        token = "API-KEY"
        dispatch {
            text {
                println(message)
                lastMessage = TelegramMessage(
                    text = message.text.orEmpty(),
                    chatId = message.chat.id
                )
            }
        }
    }

    suspend fun launch() {
        bot.startPolling()
        embeddedServer(CIO, port = 3001, host = "0.0.0.0") {
            mcp { return@mcp configureMPC() }
        }.startSuspend(wait = true)
    }

    private fun configureMPC(): Server {
        val server = Server(
            serverInfo = Implementation(
                name = "receive-telegram-message-server", version = "0.0.1"
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
        val receiveLastMessageTool = RegisteredTool(
            Tool(
                name = "get-last-message",
                description = "Возвращает последнее сообщение пользователя",
                inputSchema = Tool.Input()
            )
        ) { _ ->
            val json = Json.encodeToJsonElement(lastMessage!!)
            CallToolResult(content = listOf(TextContent(json.toString())))
        }

        val sendAnswerTool = RegisteredTool(
            Tool(
                name = "answer-on-message",
                description = "Отвечает на последние сообщение пользователя",
                inputSchema = Tool.Input(
                    properties = buildJsonObject {
                        putJsonObject("message") {
                            put("type", JsonPrimitive("string"))
                            put("description", JsonPrimitive("message, Ответ на сообщение пользователя"))
                        }
                    },
                    required = listOf("message")
                )
            )
        ) { answer ->
            val answer = answer.arguments["message"]?.jsonPrimitive?.content
            bot.sendMessage(ChatId.fromId(lastMessage!!.chatId), answer!!)
            CallToolResult(content = listOf(TextContent(answer)))
        }

        return listOf(receiveLastMessageTool, sendAnswerTool)
    }
}