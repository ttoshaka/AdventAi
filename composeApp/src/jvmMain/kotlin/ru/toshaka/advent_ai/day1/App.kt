package ru.toshaka.advent_ai.day1

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import ru.toshaka.advent_ai.day1.model.DisplayedMessage

@Composable
@Preview
fun App(
    messages: List<DisplayedMessage>,
    onSendMessage: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }

    MaterialTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(1f))
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
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
                    keyboardActions = KeyboardActions(
                        onSend = {
                            onSendMessage(text)
                            text = ""
                        }
                    )
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
    }
}