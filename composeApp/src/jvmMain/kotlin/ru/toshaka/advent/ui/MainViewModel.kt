package ru.toshaka.advent.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import ru.toshaka.advent.data.DeepSeekApi
import ru.toshaka.advent.data.model.Type

class MainViewModel {

    private val deepSeekApi = DeepSeekApi()
    private val viewModelScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val json = Json {
        classDiscriminator = "type"
    }

    val chatItems get() = _chatItems.asStateFlow()
    private val _chatItems = MutableStateFlow<List<ChatItem>>(emptyList())

    fun onSendMessageClick(text: String) {
        val item = ChatItem.ChatMessage(
            authorName = "Me",
            messageText = text,
            debugInfo = null,
            isOwnMessage = true
        )
        addChatItem(item)
        viewModelScope.launch {
            val response = deepSeekApi.sendChat(text).choices.first().message.content
            val message = json.decodeFromString<Type>(response).toChatItem()
            addChatItem(message)
        }
    }

    private fun addChatItem(item: ChatItem) {
        _chatItems.value += item
    }
}