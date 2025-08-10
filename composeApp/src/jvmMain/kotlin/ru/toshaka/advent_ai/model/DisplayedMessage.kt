package ru.toshaka.advent_ai.model

data class DisplayedMessage(
    val text: String,
    val author: Author,
) {

    enum class Author {
        USER,
        AI
    }
}