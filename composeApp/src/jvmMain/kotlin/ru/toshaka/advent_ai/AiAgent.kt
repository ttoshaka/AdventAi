package ru.toshaka.advent_ai

import ru.toshaka.advent_ai.network.model.ChatResult
import ru.toshaka.advent_ai.network.model.Message
import ru.toshaka.advent_ai.network.model.Roles
import ru.toshaka.advent_ai.network.repository.ChatDeepSeekRepository

class AiAgent(
    initialSystemPrompt: String,
    private val chatRepository: ChatDeepSeekRepository,
    private val name: String,
) {

    private val messageHistory = mutableListOf<Message>().apply {
        add(
            Message(
                role = Roles.system.name,
                content = initialSystemPrompt
            )
        )
    }

    suspend fun chat(prompt: String): ChatResult {
        val userMessage = Message(
            role = Roles.user.name,
            content = prompt
        )
        messageHistory.add(userMessage)
        try {
            val response = chatRepository.chat(messageHistory)
            val assistantMessage = Message(
                role = Roles.assistant.name,
                content = response,
            )
            messageHistory.add(assistantMessage)
            return ChatResult.Success(
                message = response,
                agentName = name,
            )
        } catch (throwable: Throwable) {
            messageHistory.remove(userMessage)
            return ChatResult.Failure(
                throwable = throwable,
                agentName = name,
            )
        }
    }
}