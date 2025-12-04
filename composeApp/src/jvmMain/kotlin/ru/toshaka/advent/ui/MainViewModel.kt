package ru.toshaka.advent.ui

import androidx.compose.ui.graphics.Color
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.*
import ru.toshaka.advent.Question
import ru.toshaka.advent.data.agent.*
import ru.toshaka.advent.data.db.AppDatabase
import ru.toshaka.advent.data.db.agent.AgentEntity
import ru.toshaka.advent.data.db.agent.AgentRepository
import ru.toshaka.advent.data.db.chat.ChatEntity
import ru.toshaka.advent.data.db.chat.ChatRepository
import ru.toshaka.advent.data.db.message.MessageEntity
import ru.toshaka.advent.data.db.message.MessagesRepository
import ru.toshaka.advent.data.db.message.isUser
import ru.toshaka.advent.mcp.McpManager
import java.io.File
import kotlin.random.Random

class MainViewModel {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val database = createDatabase()
    private val messageRepo = MessagesRepository(database.getMessageDao())
    private val chatRepo = ChatRepository(database.getChatDao())
    private val agentRepo = AgentRepository(database.getAgentDao())
    private val mcpManager = McpManager()

    private val _state = MutableStateFlow(MainScreenState.Empty)
    val state: StateFlow<MainScreenState> = _state.asStateFlow()

    private val summarizerAgent = Agent(Summarizer)

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
            mcpManager.launchServer()
        }
        observeChatsAndMessages()
    }

    fun sendQuestion(question: Question) = scope.launch {
        val id = Random.nextLong()
        saveMessage(
            MessageEntity(
                chatId = id,
                owner = 0L,
                content = question.question,
                debugInfo = null,
                timestamp = System.currentTimeMillis(),
                history = true,
            )
        )
        val agentEntity = agentRepo.getAll().first()
        val agent = Agent(
            DeepSeekChatAgent {
                this.systemPrompt = agentEntity.systemPrompt
                history = { messageRepo.getAll(id) }
                this.tools = mcpManager.getTools()
                onToolCall = { toolCall ->
                    messageRepo.save(
                        MessageEntity(
                            chatId = id,
                            owner = agentEntity.id,
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
                    messageRepo.save(
                        MessageEntity(
                            chatId = id,
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
            }
        )
        println(agent.request(question.question))
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
            val response = (summarizerAgent.request(messages.toString()) as AiResponse.TextResponse).content
            messageRepo.clear()
            val chat = chatRepo.getAll().find { it.id == chatId }!!
            val agents = agentRepo.getAll().filter { chat.agents.contains(it.id) }
            agents.forEach { agent ->
                val system = """
                            ${agent.systemPrompt}
                            Сжатая информация о предыдущем разговоре:
                            $response
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
        val chatMessages = messages.filter { it.chatId == chat.id && it.toolCallId == null }

        val messageModels = chatMessages.map { msg ->
            MainScreenState.Chat.Message(
                author = if (msg.isUser) "User" else chatAgents.find { it.id == msg.owner }?.name ?: "Unknown",
                content = msg.content.orEmpty(),
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
                    //chatRepo.clear()
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

    private fun request(chatId: Long, agentEntity: AgentEntity) {
        commandExecutor.trySend {
            val systemPrompt = agentRepo.getAll().find { it.id == agentEntity.id }!!.systemPrompt
            val agent = Agent(
                DeepSeekChatAgent {
                    this.systemPrompt = systemPrompt
                    maxTokens = agentEntity.maxTokens
                    history = { messageRepo.getAll(chatId) }
                    this.tools = mcpManager.getTools()
                    onToolCall = { toolCall ->
                        messageRepo.save(
                            MessageEntity(
                                chatId = chatId,
                                owner = agentEntity.id,
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
                        messageRepo.save(
                            MessageEntity(
                                chatId = chatId,
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
                }
            )
            val response = runCatching { agent.request() }
                .getOrElse {
                    println("Ошибка при запросе к агенту ${agentEntity.name}: ${it}")
                    return@trySend
                }

            messageRepo.save(
                MessageEntity(
                    chatId = chatId,
                    owner = agentEntity.id,
                    content = response.toString(),
                    debugInfo = buildString {
                        //usage?.promptTokens?.also {
                        //    appendLine("Prompt tokens = $it")
                        //}
                        //usage?.completionTokens?.also {
                        //    appendLine("Completion tokens = $it")
                        //}
                        //usage?.totalTokens?.also {
                        //    appendLine("Total tokens = $it")
                        //}
                        //usage?.totalTime?.also {
                        //    appendLine("Total time = $it")
                        //}
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
