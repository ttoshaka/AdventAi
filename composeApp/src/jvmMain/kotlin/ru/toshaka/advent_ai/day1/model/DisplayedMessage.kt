package ru.toshaka.advent_ai.day1.model

data class DisplayedMessage(
    val text: String,
    val author: Author,
) {

    enum class Author {
        USER,
        AI
    }
}