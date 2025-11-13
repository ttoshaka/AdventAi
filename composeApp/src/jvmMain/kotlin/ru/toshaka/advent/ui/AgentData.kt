package ru.toshaka.advent.ui

data class AgentData(
    val name: String,
    val systemPrompt: String,
    val temperature: Float,
    val maxTokens: Int
)