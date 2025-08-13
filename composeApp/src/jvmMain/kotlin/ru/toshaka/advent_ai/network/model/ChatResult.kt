package ru.toshaka.advent_ai.network.model

sealed class ChatResult(open val agentName: String) {

    data class Success(
        val message: String,
        override val agentName: String
    ) : ChatResult(agentName)

    data class Failure(
        val throwable: Throwable,
        override val agentName: String
    ) : ChatResult(agentName)
}