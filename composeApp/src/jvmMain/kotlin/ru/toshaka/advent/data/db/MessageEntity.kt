package ru.toshaka.advent.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val author: String,
    val isOwnMessage: Boolean,
)