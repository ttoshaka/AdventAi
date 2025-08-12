package ru.toshaka.advent_ai.network.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.toshaka.advent_ai.network.api.DeepSeekApi
import ru.toshaka.advent_ai.network.model.ChatRequest
import ru.toshaka.advent_ai.network.model.ChatRequestResult
import ru.toshaka.advent_ai.network.model.Message
import ru.toshaka.advent_ai.network.model.Roles

class ChatDeepSeekRepository(
    systemPrompt: String,
    private val api: DeepSeekApi,
    private val ioDispatcher: CoroutineDispatcher,
) {

    private val messageHistory = mutableListOf(
        Message(
            role = Roles.system.name,
            content = systemPrompt
        )
    )

    suspend fun chat(promt: String): ChatRequestResult = withContext(ioDispatcher) {
        val userMessage = Message(
            role = Roles.user.name,
            content = promt
        )
        messageHistory.add(userMessage)
        val request = ChatRequest(
            model = MODEL,
            messages = messageHistory,
        )
        try {
            val response = api.chat(request)
            val message = response.choices.first().message.content
            val assistantMessage = Message(
                role = Roles.assistant.name,
                content = message,
            )
            messageHistory.add(assistantMessage)
            return@withContext ChatRequestResult.Success(message)
        } catch (throwable: Throwable) {
            messageHistory.remove(userMessage)
            return@withContext ChatRequestResult.Failure(throwable)
        }
    }

    companion object {

        private const val MODEL: String = "deepseek-chat"
    }
}