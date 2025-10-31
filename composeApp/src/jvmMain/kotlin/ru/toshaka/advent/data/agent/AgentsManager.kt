package ru.toshaka.advent.data.agent

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import ru.toshaka.advent.data.db.AppDatabase
import ru.toshaka.advent.data.db.MessageEntity
import ru.toshaka.advent.data.db.MessagesRepository
import ru.toshaka.advent.ui.ChatItem
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class AgentsManager {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val database = getRoomDatabase()
    private val messageRepository = MessagesRepository(database.getDao())
    private val buss = MessageBus()
    private val agents: MutableList<Agent<*>> = mutableListOf()

    init {
        scope.launch {
            buss.flow.collect { (response, debugInfo) ->
                agents.forEach { agent ->
                    val receiveType = agent.receiveType
                    if (receiveType != null && response.javaClass == receiveType.java && debugInfo.agentName != agent.name) {
                        val message = MessageEntity(
                            content = response.toString(),
                            author = debugInfo.agentName,
                            role = MessageEntity.Roles.user,
                            chatId = agent.name,
                            debugInfo = null,
                        )
                        saveMessage(message)
                        agent.invoke(response.toString())
                    }
                }
            }
        }
    }

    fun clear() {
        scope.launch {
            messageRepository.clear()
        }
    }

    fun <R : AiResponse> addAgent(config: AgentConfig<R>): Flow<List<ChatItem>> {
        val agent = Agent(config.apply {
            onAiResponse = { response, debugInfo ->
                buss.send(response, debugInfo)
                saveMessage(
                    MessageEntity(
                        content = response.toString(),
                        author = config.name,
                        role = MessageEntity.Roles.assistant,
                        chatId = config.name,
                        debugInfo = buildString {
                            appendLine("promptToken = ${debugInfo.promptToken}")
                            appendLine("completionToken = ${debugInfo.completionToken}")
                            appendLine("totalToken = ${debugInfo.totalToken}")
                            appendLine("Time = ${System.currentTimeMillis().toReadableDateTime()}")
                        },
                    )
                )
            }
            history = messageHistory(config.name, messageRepository)
        }).apply(agents::add)

        return messageRepository.getAllAsFlow(agent.name)
    }

    fun onUserMessage(text: String) {
        agents.filter { it.isReceiveUserMessage }.forEach { agent ->
            saveMessage(
                MessageEntity(
                    content = text,
                    author = "User",
                    role = MessageEntity.Roles.user,
                    chatId = agent.name,
                    debugInfo = null,
                )
            )
            agent.invoke(text)
        }
    }

    private fun saveMessage(message: MessageEntity) {
        scope.launch {
            messageRepository.save(message)
        }
    }

    fun Long.toReadableDateTime(
        zone: ZoneId = ZoneId.systemDefault(),
        pattern: String = "dd.MM.yyyy HH:mm:ss"
    ): String {
        val formatter = DateTimeFormatter.ofPattern(pattern).withLocale(Locale.getDefault()).withZone(zone)
        return formatter.format(Instant.ofEpochMilli(this))
    }

    private fun getRoomDatabase(): AppDatabase =
        Room.databaseBuilder<AppDatabase>(File(System.getProperty("java.io.tmpdir"), "my_room.db").absolutePath)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()

    private fun messageHistory(
        chatId: String,
        messageRepository: MessagesRepository
    ): () -> List<Pair<String, String>> = {
        runBlocking {
            val messages = messageRepository.getAll(chatId)
            messages.map {
                when (it) {
                    is ChatItem.ChatMessage -> run { if (it.isOwnMessage) "user" else "assistant" } to it.messageText
                }
            }
        }
    }
}