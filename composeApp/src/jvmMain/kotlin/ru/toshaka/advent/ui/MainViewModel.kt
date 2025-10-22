package ru.toshaka.advent.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import ru.toshaka.advent.data.DeepSeekApi
import ru.toshaka.advent.data.db.MessagesRepository

class MainViewModel(
    private val id: Long,
    val model: String,
    private val deepSeekApi: DeepSeekApi,
    private val messageRepository: MessagesRepository,
) {

    private val viewModelScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val json = Json {
        classDiscriminator = "type"
    }

    val chatItems get() = messageRepository.getAllAsFlow(id)

    fun onSendMessageClick(text: String) {
        val item = ChatItem.ChatMessage(
            authorName = "Me",
            messageText = text,
            debugInfo = null,
            isOwnMessage = true
        )
        viewModelScope.launch {
            addChatItem(item)
            val previousMessages = messageRepository.getAll(id).map {
                when (it) {
                    is ChatItem.ChatMessage -> it.messageText to if (it.isOwnMessage) "user" else "assistant"
                }
            }
            val response = deepSeekApi.sendChat(previousMessages, model)
            val usage = response.usage!!
            val message = ChatItem.ChatMessage(
                authorName = "Ai",
                messageText = response.choices.first().message.content,
                debugInfo = buildString {
                    append("totalTokens ${usage.totalTokens}\n")
                    append("promptTokens ${usage.promptTokens}\n")
                    append("completionTokens ${usage.completionTokens}\n")
                    append("totalTime ${usage.totalTime}")
                },
                isOwnMessage = false
            )
            addChatItem(message)
        }
    }

    fun onClearClick() {
        viewModelScope.launch {
            messageRepository.clear()
        }
    }

    private suspend fun addChatItem(item: ChatItem) {
        messageRepository.save(item, id)
    }

}