package ru.toshaka.advent_ai.model

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed interface Element {

    @Composable
    fun toCompose()

    data class Text(
        val message: String,
        val color: String,
        val backgroundColor: String,
    ) : Element {

        @Composable
        override fun toCompose() {
            Box(
                modifier = Modifier.background(
                    color = rgbaStringToColor(backgroundColor)
                )
                    .padding(4.dp)
            ) {
                Text(
                    text = message,
                    fontSize = 24.sp,
                    color = rgbaStringToColor(color)
                )
            }
        }
    }

    data class Button(
        val message: String,
        val color: String,
        val textColor: String,
    ) : Element {

        @Composable
        override fun toCompose() {
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = rgbaStringToColor(color)
                ),
                content = {
                    Text(
                        text = message,
                        fontSize = 16.sp,
                        color = rgbaStringToColor(textColor)
                    )
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