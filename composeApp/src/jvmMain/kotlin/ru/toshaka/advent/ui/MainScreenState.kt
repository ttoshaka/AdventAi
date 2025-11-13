package ru.toshaka.advent.ui

import androidx.compose.ui.graphics.Color

data class MainScreenState(
    val chats: List<Chat>,
    val availableAgents: List<Agent>,
    val onSaveAgent: (AgentData) -> Unit,
    val onSaveChat: (agents: List<Long>, String) -> Unit,
) {
    data class Chat(
        val id: Long,
        val name: String,
        val messages: List<Message>,
        val onSendClick: (String) -> Unit,
        val onClearClick: () -> Unit,
        val onSummarizeClick: (Long) -> Unit,
    ) {
        data class Message(
            val author: String,
            val content: String,
            val position: Position,
            val debug: String?,
            val color: Color
        ) {
            enum class Position {
                LEFT, RIGHT
            }
        }
    }

    data class Agent(
        val name: String,
        val systemPrompt: String,
        val id: Long,
        val temperature: Float,
        val maxTokens: Int,
    )

    companion object {
        val Empty = MainScreenState(emptyList(), emptyList(), { _ -> }, { _, _ -> })
    }
}

data class AgentData(
    val name: String,
    val systemPrompt: String,
    val temperature: Float,
    val maxTokens: Int
)