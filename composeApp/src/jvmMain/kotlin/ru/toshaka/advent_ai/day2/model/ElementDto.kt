package ru.toshaka.advent_ai.day2.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ElementDto {

    @Serializable
    @SerialName("text")
    data class TextDto(
        val type: String,
        val message: String,
        val color: String,
    ) : ElementDto

    @Serializable
    @SerialName("button")
    data class ButtonDto(
        val type: String,
        val message: String,
    ) : ElementDto
}