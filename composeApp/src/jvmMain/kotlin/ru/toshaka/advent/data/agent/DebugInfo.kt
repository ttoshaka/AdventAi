package ru.toshaka.advent.data.agent

data class DebugInfo(
    val promptToken: Int,
    val completionToken: Int,
    val totalToken: Int,
    val agentName: String,
)