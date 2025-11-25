package ru.toshaka.advent.data.agent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface AiResponse {

    val type: String

    @Serializable
    @SerialName("text")
    data class TextResponse(
        override val type: String = "text",
        @SerialName("content")
        @FieldDescription("Обычный текстовый ответ")
        val content: String,
    ) : AiResponse {

        override fun toString(): String = content
    }

    @Serializable
    @SerialName("question")
    data class QuestionResponse(
        override val type: String = "question",
        @SerialName("question")
        @FieldDescription("Текст вопроса")
        val question: String,
    ) : AiResponse {

        override fun toString(): String = question
    }

    @Serializable
    @SerialName("kotlin")
    data class KotlinCodeResponse(
        override val type: String = "kotlin",
        @SerialName("code")
        @FieldDescription("Код на языке kotlin")
        val code: String,
    ) : AiResponse {

        override fun toString(): String = code
    }
}
