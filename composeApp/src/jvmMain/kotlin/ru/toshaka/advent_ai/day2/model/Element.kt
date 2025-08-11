package ru.toshaka.advent_ai.day2.model

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

sealed interface Element {

    @Composable
    fun toCompose()

    data class Text(
        val message: String,
        val color: String,
    ) : Element {

        @Composable
        override fun toCompose() {
            Text(
                text = message,
                color = rgbaStringToColor(color)
            )
        }
    }

    data class Button(
        val message: String,
    ) : Element {

        @Composable
        override fun toCompose() {
            Button(
                onClick = {},
                content = {
                    Text(text = message)
                }
            )
        }
    }
}

private fun rgbaStringToColor(colorString: String): Color {
    val rgba = colorString.substring(1).toULong(16).toUInt()
    val r = (rgba shr 24) and 0xFFu
    val g = (rgba shr 16) and 0xFFu
    val b = (rgba shr 8) and 0xFFu
    val a = rgba and 0xFFu
    val argb = (a.toInt() shl 24) or (r.toInt() shl 16) or (g.toInt() shl 8) or b.toInt()
    return Color(argb)
}