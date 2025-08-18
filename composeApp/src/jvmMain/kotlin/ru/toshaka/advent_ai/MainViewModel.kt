package ru.toshaka.advent_ai

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import ru.toshaka.advent_ai.mcp.client.FactClient
import ru.toshaka.advent_ai.mcp.client.TelegramBotClient
import ru.toshaka.advent_ai.mcp.server.FactServer
import ru.toshaka.advent_ai.mcp.server.TelegramBorServer
import ru.toshaka.advent_ai.model.DisplayedMessage
import ru.toshaka.advent_ai.model.ResponseType
import ru.toshaka.advent_ai.network.api.DeepSeekApi
import ru.toshaka.advent_ai.network.model.ChatResult
import ru.toshaka.advent_ai.network.repository.ChatDeepSeekRepository

class MainViewModel {

    private val viewModelScope = CoroutineScope(Dispatchers.Default)

    private val _messages = MutableStateFlow<List<DisplayedMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val botServer = TelegramBorServer()
    private val telegramClient = TelegramBotClient()

    private val factServer = FactServer()
    private val factClient = FactClient()

    private lateinit var mainAgent: AiAgent

    init {
        viewModelScope.launch {
            botServer.launch()
        }
        viewModelScope.launch {
            factServer.launch()
        }
        viewModelScope.launch {
            delay(5000)
            factClient.connect()
            val tools = telegramClient.connect()
            val toolsDescription = buildString {
                tools.forEach {
                    append("Название инструмента - ${it.name}")
                    append("\n")
                    append("Описание инструмента - ${it.description}")
                    append("\n")
                    append("Схема входных данных - ${it.inputSchema}")
                }
            }
            mainAgent = AiAgent(
                initialSystemPrompt = getSystemPrompt(toolsDescription),
                chatRepository = ChatDeepSeekRepository(DeepSeekApi(), Dispatchers.IO),
                name = "Ai-agent"
            )
        }
    }

    fun onClick(message: String) {
        showMessage(DisplayedMessage(message, DisplayedMessage.Author.User))
        viewModelScope.launch {
            when (val chatResult = mainAgent.chat(message)) {
                is ChatResult.Success -> onChatSuccess(chatResult)
                is ChatResult.Failure -> onChatFailure(chatResult)
            }
        }
    }

    private suspend fun onChatSuccess(result: ChatResult.Success) {
        val json = Json { classDiscriminator = CLASS_DISCRIMINATOR }
        val decoded = json.decodeFromString<ResponseType>(
            result.message.replace(Regex("^```json\\s*"), "")
                .replace(Regex("\\s*```$"), "")
        )
        when (decoded) {
            is ResponseType.Tools -> {
                val result = telegramClient.call(
                    name = decoded.content.toolName,
                    arguments = Json.parseToJsonElement(decoded.content.content).jsonObject
                )
                val fact = factClient.call(result)
                showMessage(
                    DisplayedMessage(
                        text = fact,
                        author = DisplayedMessage.Author.Tool("fact-mcp")
                    )
                )
            }

            is ResponseType.TextDto -> {
                showMessage(DisplayedMessage(decoded.content, DisplayedMessage.Author.Ai(result.agentName)))
            }

            else -> {
                println("Unexpected LLM response $decoded")
            }
        }
    }

    private fun showMessage(message: DisplayedMessage) {
        _messages.value += message
    }

    private fun onChatFailure(result: ChatResult.Failure) {
        result.throwable.printStackTrace()
    }

    private fun getScheme(): String? =
        javaClass.classLoader.getResource(JSON_SCHEME_FILE_NAME)?.readText()

    private fun getSystemPrompt(tools: String): String =
        "Ты — помощник, который использует предоставленные ему инструменты для обработки запросов пользователя." +
                "Вот список доступных тебе инструментов: название, описание и формат передачи данных.\n" +
                "Если ты понимаешь что при обработке запроса пользователя ты можешь воспользоваться каким-то инструментов, " +
                tools +
                " то напиши сообщение в следующем формате: наименование инструмента \"данные которые соответствую схеме передачи данных\"" +
                "\n" +
                "Правила:\n" +
                "1. Все ответы возвращай только в формате JSON.\n" +
                "2. Верхний уровень ответа всегда содержит два поля:\n" +
                "   - \"type\": строка, определяющая тип ответа.\n" +
                "   - \"content\": данные ответа. Не оборачивай его в JSON маркеры.\n" +
                "3. Типы:\n" +
                "   - \"tools\" — если необходимо воспользоваться инструментом. В \"content\" кладётся объект, строго соответствующий схеме ${getScheme()}. Экранируй символы { и }" +
                "   - \"text\" — если нужно написать простое сообщение. В \"content\" кладётся объект, текст сообщения." +
                "4. Никогда не смешивай несколько типов в одном ответе.\n" +
                "5. При \"json\" строго соблюдай предоставленную JSON-схему.\n" +
                "6. Ответ всегда должен быть корректным JSON без комментариев и лишнего текста. Не оборачивай его в JSON маркеры." +
                "7. Никогда не используй Markdown-разметку." +
                "8. В ответе не должно быть разметки типа ```json" +
                "9. Добивайся конкретных ответов от пользователя" +
                "10. За один раз уточняй все детали только для одного элемента. В одном сообщение не выдавай вопросы по всем элементам."

    companion object {

        private const val JSON_SCHEME_FILE_NAME: String = "scheme.json"
        private const val CLASS_DISCRIMINATOR = "type"
    }
}