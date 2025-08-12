package ru.toshaka.advent_ai.network.model

sealed interface ChatRequestResult {

    data class Success(
        val message: String
    ) : ChatRequestResult

    data class Failure(
        val throwable: Throwable
    ) : ChatRequestResult
}