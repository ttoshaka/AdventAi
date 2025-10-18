package ru.toshaka.advent.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val json = Json { prettyPrint = true }
private val Text = Type.Text("Обычное текстовое сообщение. Которое не содержит вопроса к пользователю. В это поле указывай текст сообщения.")
private val Question = Type.Question("Текстовое сообщене с вопросом к пользователю. В это поле указывай текст вопроса к пользователю")
private val RootContentDescription = RootContent(
    type = "Возможные варианты: text, question.",
    json = "Здесь указаны соответствующие json-схемы для типов:\n${json.encodeToString(listOf(Text, Question))}",
)

@Serializable
private data class RootContent(
    val type: String,
    val json: String,
)

@Serializable
sealed interface Type {

    @Serializable
    @SerialName("text")
    data class Text(val content: String) : Type

    @Serializable
    @SerialName("question")
    data class Question(val content: String) : Type
}

val JsonDescription = json.encodeToString(RootContentDescription)