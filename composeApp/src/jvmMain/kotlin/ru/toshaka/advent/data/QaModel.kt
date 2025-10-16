package ru.toshaka.advent.data

import kotlinx.serialization.Serializable

@Serializable
data class QaModel(
    val question: String,
    val answer: String,
)