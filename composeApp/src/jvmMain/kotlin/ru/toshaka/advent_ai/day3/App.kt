@file:Suppress("FunctionName")

package ru.toshaka.advent_ai.day3

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import ru.toshaka.advent_ai.day3.model.DisplayedMessage
import ru.toshaka.advent_ai.day3.model.Element

@Composable
@Preview
fun App(
    messages: List<DisplayedMessage>,
    elements: List<Element>,
    onSendMessage: (String) -> Unit,
) {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.weight(1f)
            ) {
                ChatWindow(
                    modifier = Modifier.fillMaxHeight()
                        .weight(1f),
                    messages = messages,
                )
                Spacer(modifier = Modifier.width(16.dp))
                PreviewCanvas(
                    modifier = Modifier.fillMaxHeight()
                        .weight(1f),
                    elements = elements,
                )
            }
            InputMessage(
                modifier = Modifier.fillMaxWidth(),
                onSendMessage = onSendMessage,
            )
        }
    }
}

@Composable
fun ChatWindow(
    modifier: Modifier = Modifier,
    messages: List<DisplayedMessage>,
) {
    LazyColumn(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(16.dp),
            )
            .background(
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(8.dp)
    ) {
        items(messages) { message ->
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    )
            ) {
                if (message.author == DisplayedMessage.Author.USER) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(message.text)
                } else {
                    Text(message.text)
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Divider(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp))
        }
    }
}

@Composable
fun PreviewCanvas(
    modifier: Modifier = Modifier,
    elements: List<Element>,
) {
    LazyColumn(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(16.dp),
            )
            .background(
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(8.dp)
    ) {
        items(elements) { element ->
            element.toCompose()
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun InputMessage(
    modifier: Modifier = Modifier,
    onSendMessage: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    Row(
        modifier = modifier
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Введите сообщение") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = {
                onSendMessage(text)
                text = ""
            })
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = {
                if (text.isNotBlank()) {
                    onSendMessage(text)
                    text = ""
                }
            }
        ) {
            Text("Отправить")
        }
    }
}