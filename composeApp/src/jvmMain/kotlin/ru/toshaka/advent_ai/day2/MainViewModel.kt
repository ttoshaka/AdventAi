package ru.toshaka.advent_ai.day2

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import ru.toshaka.advent_ai.day2.model.Element
import ru.toshaka.advent_ai.day2.model.ElementDto
import ru.toshaka.advent_ai.network.api.DeepSeekApi
import ru.toshaka.advent_ai.network.model.ChatRequestResult
import ru.toshaka.advent_ai.network.repository.ChatDeepSeekRepository

class MainViewModel {

    private val systemPrompt =
        "Ты версталищик визуальных интерфейсов который выводит результат своей работы в json формате." +
                "На вход ты будешь получать желаемый результат для отображения на экране." +
                "Твой ответ должен соответствовать следующим правилам:" +
                "1) Ответ должен быть в JSON формате. Не оборачивай его в JSON маркеры;" +
                "2) Твой JSON должен соответствовать следующей JSON-схеме ${getScheme()};" +
                "3) Ты не должен генерировать отсутсвующие в схеме элементы;" +
                "4) Если нужно указать цвет, но пользователь его не задал, то цвет должен быть черным."

    private val viewModelScope = CoroutineScope(Dispatchers.Default)
    private val deepSeekApi = DeepSeekApi()
    private val chatRepository = ChatDeepSeekRepository(
        systemPrompt = systemPrompt,
        api = deepSeekApi,
        ioDispatcher = Dispatchers.IO,
    )

    private val _textElements = MutableStateFlow<List<Element>>(emptyList())
    val textElements = _textElements.asStateFlow()

    fun getScheme(): String? =
        javaClass.classLoader.getResource(JSON_SCHEME_FILE_NAME)?.readText()

    fun onClick(message: String) {
        viewModelScope.launch {
            when (val chatResult = chatRepository.chat(message)) {
                is ChatRequestResult.Success -> onChatSuccess(chatResult.message)
                is ChatRequestResult.Failure -> onChatFailure(chatResult.throwable)
            }
        }
    }

    private fun onChatSuccess(message: String) {
        val json = Json { classDiscriminator = CLASS_DISCRIMINATOR }
        val decoded = json.decodeFromString<List<ElementDto>>(message)
        _textElements.value = decoded.map { element ->
            when (element) {
                is ElementDto.ButtonDto -> Element.Button(
                    message = element.message
                )

                is ElementDto.TextDto -> Element.Text(
                    message = element.message,
                    color = element.color
                )
            }
        }
    }

    private fun onChatFailure(throwable: Throwable) {
        println(throwable)
    }

    companion object {

        private const val JSON_SCHEME_FILE_NAME: String = "scheme.json"
        private const val CLASS_DISCRIMINATOR = "type"
    }
}