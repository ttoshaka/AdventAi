package ru.toshaka.advent.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.toshaka.advent.data.agent.Agent
import ru.toshaka.advent.data.db.MessagesRepository

class MainViewModel(
    private val agent: Agent,
    private val messageRepository: MessagesRepository,
) {

    private val viewModelScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val agentName: String = agent.name
    val chatItems get() = messageRepository.getAllAsFlow(agentName)

    fun onSendMessageClick(text: String) {
        val item = ChatItem.ChatMessage(
            authorName = "Me",
            messageText = text,
            debugInfo = null,
            isOwnMessage = true
        )
        viewModelScope.launch {
            addChatItem(item)
            val response = agent(item.messageText)
            val usage = response.usage!!
            addChatItem(
                ChatItem.ChatMessage(
                    authorName = agentName,
                    messageText = response.choices.first().message.content,
                    debugInfo = buildString {
                        append("totalTokens ${usage.totalTokens}\n")
                        append("promptTokens ${usage.promptTokens}\n")
                        append("completionTokens ${usage.completionTokens}\n")
                        append("totalTime ${usage.totalTime}")
                    },
                    isOwnMessage = false
                )
            )
        }
    }

    fun onClearClick() {
        viewModelScope.launch {
            messageRepository.clear()
        }
    }

    private suspend fun addChatItem(item: ChatItem) {
        messageRepository.save(item, agentName)
    }
}