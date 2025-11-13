package ru.toshaka.advent.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun App(state: MainScreenState) {
    var selectedChatIndex by remember { mutableStateOf(0) }
    var showAddAgentScreen by remember { mutableStateOf(false) }
    var showCreateChatScreen by remember { mutableStateOf(false) }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding()
                .padding(8.dp)
        ) {

            // Экран добавления агента
            if (showAddAgentScreen) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xAA000000))
                        .clickable(enabled = false) {}
                ) {
                    AddAgentScreen(
                        onSave = { agentData ->
                            state.onSaveAgent(agentData)
                            showAddAgentScreen = false
                        },
                        onCancel = { showAddAgentScreen = false }
                    )
                }
            }

            // Экран создания нового чата
            if (showCreateChatScreen) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xAA000000))
                        .clickable(enabled = false) {}
                ) {
                    CreateChatScreen(
                        availableAgents = state.availableAgents,
                        onSave = { selectedAgents ->
                            state.onSaveChat(selectedAgents)
                            showCreateChatScreen = false
                        },
                        onCancel = { showCreateChatScreen = false }
                    )
                }
            }

            if (state.chats.size > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedChatIndex,
                        edgePadding = 0.dp,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.weight(1f)
                    ) {
                        state.chats.forEachIndexed { index, chat ->
                            Tab(
                                selected = selectedChatIndex == index,
                                onClick = { selectedChatIndex = index },
                                text = { Text(chat.name) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Кнопка добавления агента
                    Button(onClick = { showAddAgentScreen = true }) {
                        Text("+Agent")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Кнопка создания нового чата
                    Button(onClick = { showCreateChatScreen = true }) {
                        Text("+Chat")
                    }
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Кнопка добавления агента
                    Button(onClick = { showAddAgentScreen = true }) {
                        Text("+Agent")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Кнопка создания нового чата
                    Button(onClick = { showCreateChatScreen = true }) {
                        Text("+Chat")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Отображаем только выбранный чат
            if (state.chats.isNotEmpty()) {
                ChatWindow(
                    chat = state.chats[selectedChatIndex],
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Нет чатов. Создайте нового агента или чат.",
                        color = Color.Gray
                    )
                }
            }


        }
    }
}

@Composable
private fun ChatWindow(
    chat: MainScreenState.Chat,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = chat.name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            chat.messages.forEach { message ->
                ChatMessageItem(message)
            }
        }

        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ChatInputBar(
                modifier = Modifier.weight(1f),
                onSendClick = chat.onSendClick,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = chat.onClearClick) {
                Text("Очистить")
            }
        }
    }
}

@Composable
private fun ChatMessageItem(
    message: MainScreenState.Chat.Message,
    maxWidthFraction: Float = 0.6f,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = message.position.toAlignment()
    ) {
        val messageMaxWidth = maxWidth * maxWidthFraction

        Column(
            modifier = Modifier
                .widthIn(max = messageMaxWidth)
                .background(
                    color = message.color,
                    shape = RoundedCornerShape(12.dp)
                )
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Text(
                text = message.author,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
            message.debug?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    modifier: Modifier,
    onSendClick: (String) -> Unit,
) {
    var message by remember { mutableStateOf("") }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(24.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = message,
            onValueChange = { message = it },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.Black),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Send,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (message.isNotBlank()) {
                        onSendClick(message)
                        message = ""
                    }
                }
            ),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            decorationBox = { innerTextField ->
                if (message.isEmpty()) {
                    Text(
                        text = "Введите сообщение...",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                innerTextField()
            }
        )

        Text(
            text = "➤",
            color = if (message.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray,
            modifier = Modifier
                .padding(start = 8.dp)
                .clickable(enabled = message.isNotBlank()) {
                    onSendClick(message)
                    message = ""
                },
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )
    }
}

private fun MainScreenState.Chat.Message.Position.toAlignment(): Alignment =
    when (this) {
        MainScreenState.Chat.Message.Position.LEFT -> Alignment.CenterStart
        MainScreenState.Chat.Message.Position.RIGHT -> Alignment.CenterEnd
    }
