package ru.toshaka.advent_ai.model

data class DisplayedMessage(
    val text: String,
    val author: Author,
) {

    sealed class Author(val displayedName: String) {

        data object User : Author("Пользователь")

        data class Ai(val name: String) : Author(name)

        data class Tool(val name: String) : Author(name)
    }
}