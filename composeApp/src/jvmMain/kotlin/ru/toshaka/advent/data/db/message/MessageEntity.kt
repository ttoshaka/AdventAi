package ru.toshaka.advent.data.db.message

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatId: Long,
    val owner: Long,
    val content: String?,
    val debugInfo: String?,
    val timestamp: Long,
    val history: Boolean,
    val toolCallIndex: Int? = null,
    val toolCallId: String? = null,
    val toolCallType: String? = null,
    val toolCallName: String? = null,
    val toolCallArguments: String? = null,
) {
    @Serializable
    data class ToolCall(
        val index: Int,
        val id: String,
        val type: String,
        val function: ToolFunction,
    )

    @Serializable
    data class ToolFunction(
        val name: String,
        val arguments: String,
    )
}

val MessageEntity.isUser get() = owner == 0L

val MessageEntity.type
    get() = when (owner) {
        -1L -> "tool"
        0L -> "user"
        else -> "assistant"
    }