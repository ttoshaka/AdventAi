package ru.toshaka.advent_ai.network.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.toshaka.advent_ai.network.api.DeepSeekApi
import ru.toshaka.advent_ai.network.model.ChatRequest
import ru.toshaka.advent_ai.network.model.Message

class ChatDeepSeekRepository(
    private val api: DeepSeekApi,
    private val ioDispatcher: CoroutineDispatcher,
) {

    suspend fun chat(messages: List<Message>): String = withContext(ioDispatcher) {
        val request = ChatRequest(
            model = MODEL,
            messages = messages,
        )
        val response = api.chat(request)
        val message = response.choices.first().message.content
        return@withContext message
    }

    companion object {

        private const val MODEL: String = "deepseek-chat"
    }
}