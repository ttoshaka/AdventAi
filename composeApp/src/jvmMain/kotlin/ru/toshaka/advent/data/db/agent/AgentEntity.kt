package ru.toshaka.advent.data.db.agent

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AgentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val systemPrompt: String,
    val temperature: Float,
    val maxTokens: Int,
)