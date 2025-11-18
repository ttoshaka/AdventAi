package ru.toshaka.advent.ui

import androidx.compose.ui.graphics.Color
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.*
import ru.toshaka.advent.data.AgentApi
import ru.toshaka.advent.data.agent.Agent
import ru.toshaka.advent.data.agent.DeepSeekChatAgent
import ru.toshaka.advent.data.db.AppDatabase
import ru.toshaka.advent.data.db.agent.AgentEntity
import ru.toshaka.advent.data.db.agent.AgentRepository
import ru.toshaka.advent.data.db.chat.ChatEntity
import ru.toshaka.advent.data.db.chat.ChatRepository
import ru.toshaka.advent.data.db.message.MessageEntity
import ru.toshaka.advent.data.db.message.MessagesRepository
import ru.toshaka.advent.data.db.message.isUser
import ru.toshaka.advent.mcp.page.PageClient
import ru.toshaka.advent.mcp.page.PageServer
import java.io.File

class MainViewModel {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val database = createDatabase()
    private val messageRepo = MessagesRepository(database.getMessageDao())
    private val chatRepo = ChatRepository(database.getChatDao())
    private val agentRepo = AgentRepository(database.getAgentDao())
    private val agentApi = AgentApi(DeepSeekChatAgent { })

    private val _state = MutableStateFlow(MainScreenState.Empty)
    val state: StateFlow<MainScreenState> = _state.asStateFlow()

    private val summarizerAgent = Agent(DeepSeekChatAgent {
        systemPrompt = """
            Ты — система сжатия информации.
            Тебе передан полный диалог между пользователем и ИИ-ассистентом.
            Твоя задача — создать краткое, связное и информативное резюме диалога, сохранив ключевые вопросы пользователя, важные ответы ассистента и итоговые выводы.
            Игнорируй приветствия, уточнения и повторяющиеся фразы.
            Если обсуждалось несколько тем — отрази их все кратко, но раздельно.
            Формат вывода:
            Краткое описание цели диалога
            Основные вопросы и ответы (по пунктам)
            Итог / решение / следующий шаг (если есть)
        """.trimIndent()
    })

    /** Последовательное выполнение команд */
    @OptIn(ObsoleteCoroutinesApi::class)
    private val commandExecutor = scope.actor<suspend () -> Unit>(
        capacity = Channel.UNLIMITED
    ) {
        for (cmd in channel) runCatching { cmd() }
            .onFailure { println("Ошибка выполнения команды: ${it.message}") }
    }

    init {
        scope.launch {
            PageServer().launch()
        }
        scope.launch {
            delay(1_000)
            println("Tools list - ${PageClient().connect()}")
        }

        scope.launch {
            val agents = agentRepo.getAll()
            _state.value = MainScreenState(
                chats = emptyList(),
                availableAgents = agents.map { agent ->
                    MainScreenState.Agent(
                        name = agent.name,
                        systemPrompt = agent.systemPrompt,
                        id = agent.id,
                        temperature = agent.temperature,
                        maxTokens = agent.maxTokens,
                    )
                },
                onSaveAgent = ::onSaveAgent,
                onSaveChat = ::onSaveChat
            )
        }
        observeChatsAndMessages()
    }

    private fun observeChatsAndMessages() = scope.launch {
        combine(
            chatRepo.getAllAsFlow(),
            messageRepo.getAllAsFlow(),
            agentRepo.getAllAsFlow()
        ) { chats, messages, agents ->
            _state.value = MainScreenState(
                chats = chats.map { chat -> buildChatState(chat, messages, agents) },
                availableAgents = agents.map { agent ->
                    MainScreenState.Agent(
                        name = agent.name,
                        systemPrompt = agent.systemPrompt,
                        id = agent.id,
                        temperature = agent.temperature,
                        maxTokens = agent.maxTokens,
                    )
                },
                onSaveAgent = ::onSaveAgent,
                onSaveChat = ::onSaveChat,
            )
        }.collect()
    }

    private fun onSummarizeClick(chatId: Long) {
        commandExecutor.trySend {
            val messages = messageRepo.getAll(chatId).map {
                "${if (it.isUser) "user" else "assistant"} - ${it.content}"
            }
            val response = summarizerAgent.invoke(messages, false)
            messageRepo.clear()
            val chat = chatRepo.getAll().find { it.id == chatId }!!
            val agents = agentRepo.getAll().filter { chat.agents.contains(it.id) }
            agents.forEach { agent ->
                val system = """
                            ${agent.systemPrompt}
                            Сжатая информация о предыдущем разговоре:
                            ${response.choices.first().message.content}
                        """.trimIndent()
                messageRepo.save(
                    MessageEntity(
                        chatId = chatId,
                        owner = agent.id,
                        content = system,
                        debugInfo = "System prompt",
                        timestamp = System.currentTimeMillis(),
                        history = false,
                    )
                )
                agentRepo.update(agent.copy(systemPrompt = system))
            }
        }
    }

    private fun onSaveAgent(agentData: AgentData) {
        scope.launch {
            agentRepo.save(
                AgentEntity(
                    name = agentData.name,
                    systemPrompt = agentData.systemPrompt,
                    temperature = agentData.temperature,
                    maxTokens = agentData.maxTokens,
                )
            )
        }
    }

    private fun onSaveChat(agents: List<Long>, name: String) {
        scope.launch {
            val chatId = chatRepo.save(ChatEntity(agents = agents, name = name))
            val chat = chatRepo.getAll().find { it.id == chatId }!!
            val agents = agentRepo.getAll().filter { chat.agents.contains(it.id) }
            agents.forEach { agent ->
                messageRepo.save(
                    MessageEntity(
                        chatId = chatId,
                        owner = agent.id,
                        content = agent.systemPrompt,
                        debugInfo = "System prompt",
                        timestamp = System.currentTimeMillis(),
                        history = false,
                    )
                )
            }
        }
    }

    private fun buildChatState(
        chat: ChatEntity,
        messages: List<MessageEntity>,
        agents: List<AgentEntity>
    ): MainScreenState.Chat {
        val chatAgents = agents.filter { it.id in chat.agents }
        val chatMessages = messages.filter { it.chatId == chat.id }

        val messageModels = chatMessages.map { msg ->
            MainScreenState.Chat.Message(
                author = if (msg.isUser) "User" else chatAgents.find { it.id == msg.owner }?.name ?: "Unknown",
                content = msg.content,
                position = if (msg.isUser)
                    MainScreenState.Chat.Message.Position.RIGHT
                else
                    MainScreenState.Chat.Message.Position.LEFT,
                debug = msg.debugInfo,
                color = if (msg.isUser) Color(0xFFDFFFD6) else Color.White
            )
        }

        return MainScreenState.Chat(
            id = chat.id,
            name = chat.name,
            messages = messageModels,
            onSendClick = { content -> onUserMessage(chat.id, content, chatAgents) },
            onClearClick = {
                commandExecutor.trySend {
                    messageRepo.clear()
                    chatRepo.clear()
                }
            },
            onSummarizeClick = { chatId -> onSummarizeClick(chatId) },
        )
    }

    private fun onUserMessage(chatId: Long, content: String, agents: List<AgentEntity>) {
        scope.launch {
            saveMessage(
                MessageEntity(
                    chatId = chatId,
                    owner = 0L,
                    content = content,
                    debugInfo = null,
                    timestamp = System.currentTimeMillis(),
                    history = true,
                )
            )
            agents.forEach { request(chatId, it) }
        }
    }

    private fun saveMessage(message: MessageEntity) {
        commandExecutor.trySend { messageRepo.save(message) }
    }

    private fun request(chatId: Long, agent: AgentEntity) {
        commandExecutor.trySend {
            val messages = messageRepo.getAll(chatId)
            val agent = agentRepo.getAll().find { it.id == agent.id }!!
            val dialog = messages.filter { it.history && (it.isUser || it.owner == agent.id) }

            val response = runCatching {
                agentApi.send(agent.systemPrompt, agent.maxTokens, dialog)
            }.getOrElse {
                println("Ошибка при запросе к агенту ${agent.name}: ${it.message}")
                return@trySend
            }

            val answer = response.choices.firstOrNull() ?: return@trySend
            val usage = response.usage
            messageRepo.save(
                MessageEntity(
                    chatId = chatId,
                    owner = agent.id,
                    content = answer.message.content,
                    debugInfo = buildString {
                        usage?.promptTokens?.also {
                            appendLine("Prompt tokens = $it")
                        }
                        usage?.completionTokens?.also {
                            appendLine("Completion tokens = $it")
                        }
                        usage?.totalTokens?.also {
                            appendLine("Total tokens = $it")
                        }
                        usage?.totalTime?.also {
                            appendLine("Total time = $it")
                        }
                    },
                    timestamp = System.currentTimeMillis(),
                    history = true,
                )
            )
        }
    }

    private fun createDatabase(): AppDatabase =
        Room.databaseBuilder<AppDatabase>(
            File(System.getProperty("java.io.tmpdir"), "my_room.db").absolutePath
        )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
}
