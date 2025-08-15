package ru.toshaka.advent_ai

import io.ktor.client.*
import io.ktor.client.plugins.sse.*
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.client.ClientOptions
import io.modelcontextprotocol.kotlin.sdk.client.SseClientTransport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import ru.toshaka.advent_ai.model.DisplayedMessage
import ru.toshaka.advent_ai.model.Element
import ru.toshaka.advent_ai.model.ResponseType
import ru.toshaka.advent_ai.network.api.DeepSeekApi
import ru.toshaka.advent_ai.network.model.ChatResult
import ru.toshaka.advent_ai.network.repository.ChatDeepSeekRepository

class MainViewModel {

    private val client = Client(
        clientInfo = Implementation("chat-ui", "0.0.1"),
        options = ClientOptions()
    )
    private val httpClient = HttpClient {
        install(SSE)
    }
    private val transport = SseClientTransport(
        client = httpClient,
        urlString = "http://localhost:3001"
    )

    private lateinit var layoutAgent: AiAgent

    private val deepSeekApi = DeepSeekApi()
    private val viewModelScope = CoroutineScope(Dispatchers.Default)

    private val _messages = MutableStateFlow<List<DisplayedMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    init {
        viewModelScope.launch {
            launchClient { toolsDescription ->
                layoutAgent = AiAgent(
                    chatRepository = ChatDeepSeekRepository(
                        api = deepSeekApi,
                        ioDispatcher = Dispatchers.IO
                    ),
                    initialSystemPrompt = getSystemPrompt(toolsDescription),
                    name = "AI-помошник"
                )
            }
        }
    }

    fun onClick(message: String) {
        showMessage(DisplayedMessage(message, DisplayedMessage.Author.User))
        viewModelScope.launch {
            when (val chatResult = layoutAgent.chat(message)) {
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
                val aiMessage = DisplayedMessage(
                    text = result.message,
                    author = DisplayedMessage.Author.Ai(result.agentName),
                )
                showMessage(aiMessage)

                val result = client.callTool(
                    name = decoded.content.toolName,
                    arguments = Json.parseToJsonElement(decoded.content.content).jsonObject
                )
                result?.content?.forEach { content ->
                    when (content) {
                        is TextContent -> {
                            val toolMessage = DisplayedMessage(
                                text = content.text ?: "Empty text",
                                author = DisplayedMessage.Author.Tool("MCP tool - ${decoded.content.toolName}"),
                            )
                            showMessage(toolMessage)
                        }

                        else -> {
                            println("Unexpected MCP tool response $decoded")
                        }
                    }
                }
            }

            else -> {
                println("Unexpected LLM response $decoded")
            }
        }
    }

    private fun showMessage(message: DisplayedMessage) {
        _messages.value = _messages.value + message
    }

    private fun onChatFailure(result: ChatResult.Failure) {
        result.throwable.printStackTrace()
    }

    private fun getScheme(): String? =
        javaClass.classLoader.getResource(JSON_SCHEME_FILE_NAME)?.readText()


    private suspend fun launchClient(onToolsReceived: (String) -> Unit) {
        client.connect(transport)
        val tools = client.listTools()
        val toolsDescription = buildString {
            tools!!.tools.forEach {
                append("Название инструмента - ${it.name}")
                append("\n")
                append("Описание инструмента - ${it.description}")
                append("\n")
                append("Схема входных данных - ${it.inputSchema}")
            }
        }
        onToolsReceived(toolsDescription)
    }

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