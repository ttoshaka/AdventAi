package ru.toshaka.advent_ai.day3.model

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
        val color: String,
    ) : ElementDto
}

@Serializable
sealed interface ResponseType {

    @Serializable
    @SerialName("text")
    data class TextDto(
        val type: String,
        val content: String,
    ) : ResponseType

    @Serializable
    @SerialName("question")
    data class QuestionDto(
        val type: String,
        val content: String,
    ) : ResponseType

    @Serializable
    @SerialName("json")
    data class JsonDto(
        val type: String,
        val content: List<ElementDto>,
    ) : ResponseType
}