package ru.toshaka.advent_ai.day2

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import ru.toshaka.advent_ai.day2.model.Element
import ru.toshaka.advent_ai.day2.model.ElementDto
import ru.toshaka.advent_ai.network.DeepSeekClientApi

class MainViewModel {

    private val viewModelScope = CoroutineScope(Dispatchers.Default)
    private val clientApi = DeepSeekClientApi(
        system = "Ты версталищик визуальных интерфейсов который выводит результат своей работы в json формате." +
                "На вход ты будешь получать желаемый результат для отображения на экране." +
                "Твой ответ должен соответствовать следующим правилам:" +
                "1) Ответ должен быть в JSON формате. Не оборачивай его в JSON маркеры;" +
                "2) Твой JSON должен соответствовать следующей JSON-схеме ${getScheme()};" +
                "3) Ты не должен генерировать отсутсвующие в схеме элементы."
    )

    private val _textElements = MutableStateFlow<List<Element>>(emptyList())
    val textElements = _textElements.asStateFlow()

    fun getScheme(): String? =
        javaClass.classLoader.getResource("scheme.json")?.readText()

    fun onClick(message: String) {
        viewModelScope.launch {
            val response = clientApi.chat(message)
            val json = Json { classDiscriminator = "type" }
            val decoded = json.decodeFromString<List<ElementDto>>(response)
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
    }
}