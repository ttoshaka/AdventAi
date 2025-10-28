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
import ru.toshaka.advent.mcp.Client
import ru.toshaka.advent.mcp.ObsidianClient
import ru.toshaka.advent.mcp.ObsidianServer
import ru.toshaka.advent.mcp.Server

class MainViewModel {

    private val viewModelScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val agentsManager = AgentsManager()

    private val _state = MutableStateFlow(MainScreenState.Empty)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val server = ObsidianServer()
            server.launch()
        }
        viewModelScope.launch {
            delay(5_000)
            val client = ObsidianClient()
            val tools = client.connect()
            createAgent(
                DeepSeekChatAgent {
                    name = "Default agent 1"
                    systemPrompt = """
                        Ты AI-ассистент.
                        В твоем распоряжение есть набор инструментов. В случае если один из инструментов подходит для выполнения запроса пользователя, то ты должен воспользоваться им, ответив соответствующим форматом сообщения.
                        В ответ от инструмента ты получишь результат работы, который ты должен передать пользователю.
                        Список инструментов:
                        ${tools.toPrompt()}
                    """.trimIndent()
                    outputFormats = listOf(
                        AiResponse.TextResponse::class,
                        AiResponse.ToolCall::class
                    )
                    isReceiveUserMessage = true
                    this.tools = { q, w ->
                        runBlocking { client.call(q, w) }
                    }
                }
            )
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
