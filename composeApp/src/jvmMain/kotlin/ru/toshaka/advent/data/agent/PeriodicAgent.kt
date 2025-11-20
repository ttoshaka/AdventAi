package ru.toshaka.advent.data.agent

import kotlinx.coroutines.delay
import ru.toshaka.advent.data.db.message.MessageEntity
import ru.toshaka.advent.mcp.McpManager

class PeriodicAgent(
    private val mcpManager: McpManager,
) {

    private val localHistory = mutableListOf<MessageEntity>()

    suspend fun start() {
        val agent = Agent(DeepSeekChatAgent {
            this.systemPrompt = systemPrompt
            maxTokens = 8000
            history = { localHistory }
            temperature = 0.1f
            this.tools = mcpManager.getTools()
            onToolCall = { toolCall ->
                localHistory.add(
                    MessageEntity(
                        chatId = -100,
                        owner = -100,
                        content = null,
                        toolCallIndex = toolCall.index,
                        toolCallId = toolCall.id,
                        toolCallType = toolCall.type,
                        toolCallName = toolCall.function.name,
                        toolCallArguments = toolCall.function.arguments,
                        debugInfo = null,
                        timestamp = System.currentTimeMillis(),
                        history = true
                    )
                )
                val toolResponse = mcpManager.callTool(toolCall.function.name, toolCall.function.arguments)
                localHistory.add(
                    MessageEntity(
                        chatId = -100,
                        owner = -1L,
                        content = toolResponse,
                        toolCallId = toolCall.id,
                        debugInfo = null,
                        timestamp = System.currentTimeMillis(),
                        history = true
                    )
                )
                toolResponse
            }
        })

        while (true) {
            localHistory.add(
                MessageEntity(
                    chatId = -100,
                    owner = 0,
                    content = """
                        Твоя задача получить список новостей, сделать краткий пересказ каждой новости и сохранить всё в файл.
                        Твой алгоритм работы следующий:
                        1)Получить содержимое страницы https://dtf.ru/rss
                        2)В полученном содержимом будет список новостей. У каждой новости указанна ссылка на детальное описание. Тебе нужно собрать список всех ссылок со всех новостей;
                        3)Получить содержимое всех ссылок;
                        4)Сформулировать краткий пересказ каждой новости;
                        5)Получить текущее время в формате yyyy-MM-dd_HH.mm.ss;
                        6)Сохранить краткий пересказ в файл, в качестве название должно быть время полученно на предыдущем шаге.
                        
                        Игнорируй что ссылка является RSS-лентой, работай с ней как с обычным сайтом.
                    """.trimIndent(),
                    debugInfo = null,
                    timestamp = System.currentTimeMillis(),
                    history = true,
                    toolCallIndex = null,
                    toolCallId = null,
                    toolCallType = null,
                    toolCallName = null,
                    toolCallArguments = null,
                )
            )
            val response = agent.request()
            println(response)
            localHistory.clear()
            delay(2 * 60 * 1000)
        }
    }
}