package ru.toshaka.advent_ai

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import ru.toshaka.advent_ai.model.DisplayedMessage
import ru.toshaka.advent_ai.model.Element
import ru.toshaka.advent_ai.model.ElementDto
import ru.toshaka.advent_ai.model.ResponseType
import ru.toshaka.advent_ai.network.api.DeepSeekApi
import ru.toshaka.advent_ai.network.model.ChatResult
import ru.toshaka.advent_ai.network.repository.ChatDeepSeekRepository

class MainViewModel {

    private val systemPromptLayout: String =
        "Ты — помощник, который создаёт разметку экрана в формате, который отвечает строго в структурированном формате JSON.\n" +
                "Задавай вопросы пользователю, пока не получишь в данные для построения экрана в JSON формате, схема которого будет указана далее.\n" +
                "\n" +
                "Правила:\n" +
                "1. Все ответы возвращай только в формате JSON.\n" +
                "2. Верхний уровень ответа всегда содержит два поля:\n" +
                "   - \"type\": строка, определяющая тип ответа.\n" +
                "   - \"content\": данные ответа. Не оборачивай его в JSON маркеры.\n" +
                "3. Типы:\n" +
                "   - \"question\" — если нужно уточнить недостающую информацию. В \"content\" кладётся только текст вопроса.\n" +
                "   - \"json\" — если готов результат по предоставленной JSON-схеме\n${getScheme()}\n. В \"content\" кладётся объект, строго соответствующий схеме. Не оборачивай его в JSON маркеры. Никогда не используй Markdown-разметку. В ответе не должно быть разметки типа ```json. Только сырой json\"\n" +
                "   - \"text\" — произвольный текстовый ответ. В \"content\" кладётся только текст.\n" +
                "4. Никогда не смешивай несколько типов в одном ответе.\n" +
                "5. При \"json\" строго соблюдай предоставленную JSON-схему.\n" +
                "6. Ответ всегда должен быть корректным JSON без комментариев и лишнего текста. Не оборачивай его в JSON маркеры." +
                "7. Никогда не используй Markdown-разметку." +
                "8. В ответе не должно быть разметки типа ```json" +
                "9. Добивайся конкретных ответов от пользователя" +
                "10. За один раз уточняй все детали только для одного элемента. В одном сообщение не выдавай вопросы по всем элементам."

    private val systemPromptDesigner: String =
        "Ты дизайнер с идеальным чувством цвета. На вход ты будешь получать разметку визуального интерфейса в формате JSON." +
                "Твоя задача проанализировать выбранные цвета и предложить их заменить на близкие, но более совместимые между собой." +
                "Учитывай что этот интерфейс в дальнейшем будет отображаться на белом фоне." +
                "Твой ответ должен четко соответсвовать следующим правилам:" +
                "1. Ты указываешь тип элемента, его местоположение и какие цвета на какие ты хочешь заменить" +
                "2. Больше твой ответ не должен ничего содержать." +
                "3. Если считаешь что цвет гармоничны, то сообщи об этом." +
                "4. Если изменения цвета не требуется, то не пиши об этом" +
                "Пример ответа: Измени цвет первого текста на #FF0000FF, а его фон на #FF0000FF"

    private val deepSeekApi = DeepSeekApi()

    private val designerAgent: AiAgent = AiAgent(
        chatRepository = ChatDeepSeekRepository(
            api = deepSeekApi,
            ioDispatcher = Dispatchers.IO
        ),
        initialSystemPrompt = systemPromptDesigner,
        name = "AI-дизайнер"
    )
    private val layoutAgent: AiAgent = AiAgent(
        chatRepository = ChatDeepSeekRepository(
            api = deepSeekApi,
            ioDispatcher = Dispatchers.IO
        ),
        initialSystemPrompt = systemPromptLayout,
        name = "AI-верстальщик"
    )

    private val viewModelScope = CoroutineScope(Dispatchers.Default)

    private val _messages = MutableStateFlow<List<DisplayedMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _elements = MutableStateFlow<List<Element>>(emptyList())
    val elements = _elements.asStateFlow()

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
            is ResponseType.QuestionDto -> {
                val displayedMessage = DisplayedMessage(
                    text = decoded.content,
                    author = DisplayedMessage.Author.Ai(result.agentName),
                )
                showMessage(displayedMessage)
            }

            is ResponseType.TextDto -> {
                val displayedMessage = DisplayedMessage(
                    text = decoded.content,
                    author = DisplayedMessage.Author.Ai(result.agentName),
                )
                showMessage(displayedMessage)
            }

            is ResponseType.JsonDto -> {
                _elements.value = decoded.content.map { element ->
                    when (element) {
                        is ElementDto.ButtonDto -> Element.Button(
                            message = element.message,
                            color = element.color,
                            textColor = element.textColor,
                        )

                        is ElementDto.TextDto -> Element.Text(
                            message = element.message,
                            color = element.color,
                            backgroundColor = element.backgroundColor,
                        )
                    }
                }
                when (val designerResponse = designerAgent.chat(result.message)) {
                    is ChatResult.Success -> {
                        val displayedMessage = DisplayedMessage(
                            text = designerResponse.message,
                            author = DisplayedMessage.Author.Ai(designerResponse.agentName),
                        )
                        showMessage(displayedMessage)
                        when (val chatResult = layoutAgent.chat(designerResponse.message)) {
                            is ChatResult.Success -> onChatSuccess(chatResult)
                            is ChatResult.Failure -> onChatFailure(chatResult)
                        }
                    }

                    is ChatResult.Failure -> onChatFailure(designerResponse)
                }
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

    companion object {

        private const val JSON_SCHEME_FILE_NAME: String = "scheme.json"
        private const val CLASS_DISCRIMINATOR = "type"
    }
}