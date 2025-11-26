package ru.toshaka.advent.data.agent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface AiResponse {

    val type: String

    @Serializable
    @SerialName("text")
    @ResponseDescription("""
        Используется для обычного текстового сообщения.
        Пример:
            Пользователь - Расскажи интересный факт.
            ИИ-агент - У осьминогов синий цвет крови.
    """)
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
    @ResponseDescription("""
        Используется для обычного текстового сообщения.
        Пример:
            Пользователь - Задай мне вопрос.
            ИИ-агент - Как тебя зовут?.
    """)
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
    @ResponseDescription("""
        Используется для обычного текстового сообщения.
        Пример:
            Пользователь - Напиши метод на языке котлин который складывает два числа.
            ИИ-агент - fun summarizer(first: Int, second: Int): Int = first + second.
    """)
    data class KotlinCodeResponse(
        override val type: String = "kotlin",
        @SerialName("code")
        @FieldDescription("Код на языке kotlin")
        val code: String,
    ) : AiResponse {

        override fun toString(): String = code
    }
}
