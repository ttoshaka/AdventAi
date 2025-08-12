package ru.toshaka.advent_ai.day3

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import ru.toshaka.advent_ai.day3.model.DisplayedMessage
import ru.toshaka.advent_ai.day3.model.Element
import ru.toshaka.advent_ai.day3.model.ElementDto
import ru.toshaka.advent_ai.day3.model.ResponseType
import ru.toshaka.advent_ai.network.api.DeepSeekApi
import ru.toshaka.advent_ai.network.model.ChatRequestResult
import ru.toshaka.advent_ai.network.repository.ChatDeepSeekRepository

class MainViewModel {

    private val systemPrompt =
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
                "8. В ответе не должно быть разметки типа ```json"

    private val viewModelScope = CoroutineScope(Dispatchers.Default)
    private val deepSeekApi = DeepSeekApi()
    private val chatRepository = ChatDeepSeekRepository(
        systemPrompt = systemPrompt,
        api = deepSeekApi,
        ioDispatcher = Dispatchers.IO,
    )

    private val _messages = MutableStateFlow<List<DisplayedMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _elements = MutableStateFlow<List<Element>>(emptyList())
    val elements = _elements.asStateFlow()

    fun onClick(message: String) {
        showMessage(DisplayedMessage(message, DisplayedMessage.Author.USER))
        viewModelScope.launch {
            when (val chatResult = chatRepository.chat(message)) {
                is ChatRequestResult.Success -> onChatSuccess(chatResult.message)
                is ChatRequestResult.Failure -> onChatFailure(chatResult.throwable)
            }
        }
    }

    private fun onChatSuccess(message: String) {
        val json = Json { classDiscriminator = CLASS_DISCRIMINATOR }
        val decoded = json.decodeFromString<ResponseType>(
            message.replace(Regex("^```json\\s*"), "")
                .replace(Regex("\\s*```$"), "")
        )
        when (decoded) {
            is ResponseType.QuestionDto -> {
                showMessage(DisplayedMessage(decoded.content, DisplayedMessage.Author.AI))
            }

            is ResponseType.TextDto -> {
                showMessage(DisplayedMessage(decoded.content, DisplayedMessage.Author.AI))
            }

            is ResponseType.JsonDto -> {
                _elements.value = decoded.content.map { element ->
                    when (element) {
                        is ElementDto.ButtonDto -> Element.Button(
                            message = element.message,
                            color = element.color,
                        )

                        is ElementDto.TextDto -> Element.Text(
                            message = element.message,
                            color = element.color
                        )
                    }
                }
            }
        }
    }

    private fun showMessage(message: DisplayedMessage) {
        _messages.value = _messages.value + message
    }

    private fun onChatFailure(throwable: Throwable) {
        throwable.printStackTrace()
    }

    private fun getScheme(): String? =
        javaClass.classLoader.getResource(JSON_SCHEME_FILE_NAME)?.readText()

    companion object {

        private const val JSON_SCHEME_FILE_NAME: String = "scheme_v2.json"
        private const val CLASS_DISCRIMINATOR = "type"
    }
}