package ru.toshaka.advent.ui

import androidx.compose.ui.graphics.Color
import io.modelcontextprotocol.kotlin.sdk.Tool
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.toshaka.advent.data.agent.AgentConfig
import ru.toshaka.advent.data.agent.AgentsManager
import ru.toshaka.advent.data.agent.AiResponse
import ru.toshaka.advent.data.agent.DeepSeekChatAgent
import ru.toshaka.advent.mcp.code.CodeClient
import ru.toshaka.advent.mcp.code.CodeServer
import ru.toshaka.advent.mcp.console.ConsoleClient
import ru.toshaka.advent.mcp.console.ConsoleServer
import ru.toshaka.advent.mcp.obsidian.ObsidianClient
import ru.toshaka.advent.mcp.obsidian.ObsidianServer
import ru.toshaka.advent.mcp.page.PageClient
import ru.toshaka.advent.mcp.page.PageServer

class MainViewModel {

    private val viewModelScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val agentsManager = AgentsManager()

    private val _state = MutableStateFlow(MainScreenState.Empty)
    val state = _state.asStateFlow()

    private val lazyAgent = DeepSeekChatAgent {
        name = "Ленивый агент"
        systemPrompt = """
                        Ты очень ленивый AI-ассистент. Решай поставленные задачи как можно более простым методом.
                        В результате своей работы отдавай просто ответ, не нужно расписывать ход решения.
                    """.trimIndent()
        outputFormats = listOf(AiResponse.TextResponse::class)
        isReceiveUserMessage = true
    }
    private val smartAgent = DeepSeekChatAgent {
        name = "Умный агент"
        systemPrompt = """
                        Ты самый умный AI-ассистент. Решай поставленные задачи как можно более умным и сложным способом.
                    """.trimIndent()
        outputFormats = listOf(AiResponse.TextResponse::class)
        isReceiveUserMessage = true
    }
    private val stepAgent = DeepSeekChatAgent {
        name = "Пошаговый агент"
        systemPrompt = """
                        Ты AI-ассистент. Решай поставленные задачи пошагово, используя технику chain-of-thought.
                    """.trimIndent()
        outputFormats = listOf(AiResponse.TextResponse::class)
        isReceiveUserMessage = true
    }

    init {
        viewModelScope.launch {
            createAgent(lazyAgent)
            createAgent(smartAgent)
            createAgent(stepAgent)
        }
    }

    /**
     * Универсальный метод создания и подключения нового агента.
     * Добавляет его в AgentsManager, подписывается на его Flow и
     * добавляет в UI состояние чата.
     */
    private fun createAgent(
        config: AgentConfig<AiResponse>
    ) {
        val flow = agentsManager.addAgent(config)

        // Добавляем чат в UI состояние
        _state.update { oldState ->
            oldState.copy(
                chats = oldState.chats + (
                        config.name to MainScreenState.Chat(
                            name = config.name,
                            messages = emptyList(),
                            onSendClick = { agentsManager.onUserMessage(it) },
                            onClearClick = { agentsManager.clear() },
                        )
                        )
            )
        }

        // Подписываемся на обновления агента
        viewModelScope.launch {
            flow.collect(createFlowCollector(config.name))
        }
    }

    /**
     * Создаёт FlowCollector для каждого агента.
     * При получении сообщений обновляет состояние чата.
     */
    private fun createFlowCollector(agentName: String) = FlowCollector<List<ChatItem>> { chatItems ->
        _state.update { oldState ->
            val chat = oldState.chats[agentName] ?: return@update oldState

            val updatedChat = chat.copy(
                messages = chatItems.map { item ->
                    when (item) {
                        is ChatItem.ChatMessage -> MainScreenState.Chat.Message(
                            author = item.authorName,
                            content = item.messageText,
                            position = if (item.isOwnMessage)
                                MainScreenState.Chat.Message.Position.RIGHT
                            else
                                MainScreenState.Chat.Message.Position.LEFT,
                            debug = item.debugInfo,
                            color = if (item.isOwnMessage)
                                Color(0xFFDFFFD6)
                            else
                                Color(0xFFFFFFFF),
                        )
                    }
                }
            )

            oldState.copy(
                chats = oldState.chats.toMutableMap().apply {
                    this[agentName] = updatedChat
                }
            )
        }
    }

    private fun List<Tool>.toPrompt(): String =
        buildString {
            this@toPrompt.forEach { tool ->
                appendLine("Название инструмента - ${tool.name}")
                appendLine("Формат входных данных - ${tool.inputSchema}")
                appendLine()
                appendLine()
            }
        }
}
