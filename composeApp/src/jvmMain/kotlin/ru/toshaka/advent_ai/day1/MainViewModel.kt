package ru.toshaka.advent_ai.day1

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.toshaka.advent_ai.day1.model.DisplayedMessage
import ru.toshaka.advent_ai.network.api.DeepSeekApi
import ru.toshaka.advent_ai.network.model.ChatRequestResult
import ru.toshaka.advent_ai.network.repository.ChatDeepSeekRepository

class MainViewModel {

    private val viewModelScope = CoroutineScope(Dispatchers.Default)
    private val deepSeekApi = DeepSeekApi()
    private val chatRepository = ChatDeepSeekRepository(
        systemPrompt = "You are a helpful assistant.",
        api = deepSeekApi,
        ioDispatcher = Dispatchers.IO,
    )

    private val _messages = MutableStateFlow<List<DisplayedMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    fun onClick(message: String) {
        _messages.value = _messages.value + DisplayedMessage(message, DisplayedMessage.Author.USER)
        viewModelScope.launch {
            when (val chatResult = chatRepository.chat(message)) {
                is ChatRequestResult.Success -> onChatSuccess(chatResult.message)
                is ChatRequestResult.Failure -> onChatFailure(chatResult.throwable)
            }
        }
    }

    private fun onChatSuccess(message: String) {
        _messages.value = _messages.value + DisplayedMessage(message, DisplayedMessage.Author.AI)
    }

    private fun onChatFailure(throwable: Throwable) {
        println(throwable)
    }
}