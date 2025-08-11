package ru.toshaka.advent_ai.network.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatResponse(
    val id: String,
    val choices: List<Choice>
) {

    @Serializable
    data class Choice(
        val message: Message
    )
}