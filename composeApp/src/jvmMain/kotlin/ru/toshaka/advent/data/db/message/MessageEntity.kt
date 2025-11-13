package ru.toshaka.advent.data.db.message

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatId: Long,
    val owner: Long,
    val content: String,
    val debugInfo: String?,
    val timestamp: Long,
)

val MessageEntity.isUser get() = owner == 0L