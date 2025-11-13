package ru.toshaka.advent.ui

import androidx.compose.ui.graphics.Color
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.*
import ru.toshaka.advent.data.AgentApi
import ru.toshaka.advent.data.agent.DeepSeekChatAgent
import ru.toshaka.advent.data.db.AppDatabase
import ru.toshaka.advent.data.db.agent.AgentEntity
import ru.toshaka.advent.data.db.agent.AgentRepository
import ru.toshaka.advent.data.db.chat.ChatEntity
import ru.toshaka.advent.data.db.chat.ChatRepository
import ru.toshaka.advent.data.db.message.MessageEntity
import ru.toshaka.advent.data.db.message.MessagesRepository
import ru.toshaka.advent.data.db.message.isUser
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeChatsAndMessages() = scope.launch {
        val agents = agentRepo.getAll()
        combine(
            chatRepo.getAllAsFlow(),
            messageRepo.getAllAsFlow(),
            agentRepo.getAllAsFlow()
        ) { chats, messages, agents ->
            _state.value = MainScreenState(
                chats = chats.map { chat ->
                    buildChatState(chat, messages.filter { it.chatId == chat.id }, agents)
                },
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
        }.collect()
        chatRepo.getAllAsFlow()
            .flatMapLatest { chats ->
                combine(chats.map { chat ->
                    messageRepo.getAllAsFlow(chat.id)
                        .map { messages -> chat to messages }
                }) { it.toList() }
            }
            .collect { chatWithMessages ->
                _state.value = MainScreenState(
                    chatWithMessages.map { (chat, messages) ->
                        buildChatState(chat, messages, agents)
                    },
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

    private fun onSaveChat(agents: List<Long>) {
        scope.launch {
            chatRepo.save(
                ChatEntity(
                    agents = agents
                )
            )
        }
    }

    private fun buildChatState(
        chat: ChatEntity,
        messages: List<MessageEntity>,
        agents: List<AgentEntity>
    ): MainScreenState.Chat {
        val chatAgents = agents.filter { it.id in chat.agents }

        val messageModels = messages.map { msg ->
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
            name = chatAgents.joinToString { it.name },
            messages = messageModels,
            onSendClick = { content -> onUserMessage(chat.id, content, chatAgents) },
            onClearClick = {
                commandExecutor.trySend {
                    messageRepo.clear()
                    chatRepo.clear()
                }
            }
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
                    timestamp = System.currentTimeMillis()
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
            val dialog = messages.filter { it.isUser || it.owner == agent.id }

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
                    timestamp = System.currentTimeMillis()
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
