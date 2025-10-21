package ru.toshaka.advent.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import ru.toshaka.advent.data.DeepSeekApi
import ru.toshaka.advent.data.db.MessagesRepository
import ru.toshaka.advent.data.model.Type

class MainViewModel(
    private val id: Long,
    val temp: Float,
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
            val response = deepSeekApi.sendChat(previousMessages, temp)
            val tokens = response.usage!!
            val message = json.decodeFromString<Type>(response.choices.first().message.content).toChatItem(
                buildString {
                    append("totalTokens ${tokens.totalTokens}\n")
                    append("promptTokens ${tokens.promptTokens}\n")
                    append("completionTokens ${tokens.completionTokens}")
                }
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