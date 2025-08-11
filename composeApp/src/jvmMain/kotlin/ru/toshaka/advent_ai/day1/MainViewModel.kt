package ru.toshaka.advent_ai.day1

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.toshaka.advent_ai.day1.model.DisplayedMessage
import ru.toshaka.advent_ai.network.DeepSeekClientApi

class MainViewModel {

    private val viewModelScope = CoroutineScope(Dispatchers.Default)
    private val clientApi = DeepSeekClientApi()

    private val _messages = MutableStateFlow<List<DisplayedMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    fun onClick(message: String) {
        _messages.value = _messages.value + DisplayedMessage(message, DisplayedMessage.Author.USER)
        viewModelScope.launch {
            val response = clientApi.chat(message)
            _messages.value = _messages.value + DisplayedMessage(response, DisplayedMessage.Author.AI)
        }
    }
}