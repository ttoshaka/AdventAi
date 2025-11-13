package ru.toshaka.advent.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.toshaka.advent.ui.MainScreenState

@Composable
fun App(state: MainScreenState) {
    var selectedChatIndex by remember(key1 = state.chats.size) { mutableStateOf(0) }
    var showAddAgentScreen by remember { mutableStateOf(false) }
    var showCreateChatScreen by remember { mutableStateOf(false) }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(top = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(end = 8.dp)
            ) {
                if (state.chats.isNotEmpty()) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedChatIndex,
                        edgePadding = 0.dp,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
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
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TopBarActionSymbol("+", { showCreateChatScreen = true })
                    TopBarActionSymbol("⚙", { showAddAgentScreen = true })
                }
            }
            // === Модальные окна ===
            if (showAddAgentScreen) {
                ModalOverlay {
                    AddAgentScreen(
                        onSave = {
                            state.onSaveAgent(it)
                            showAddAgentScreen = false
                        },
                        onCancel = { showAddAgentScreen = false }
                    )
                }
            }

            if (showCreateChatScreen) {
                ModalOverlay {
                    CreateChatScreen(
                        availableAgents = state.availableAgents,
                        onSave = { agents, name ->
                            state.onSaveChat(agents, name)
                            showCreateChatScreen = false
                        },
                        onCancel = { showCreateChatScreen = false }
                    )
                }
            }

            // === Содержимое ===
            Spacer(Modifier.height(8.dp))

            if (state.chats.isNotEmpty()) {
                ChatWindow(
                    chat = state.chats[selectedChatIndex],
                    modifier = Modifier.weight(1f)
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
private fun TopBarActionSymbol(
    symbol: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun ModalOverlay(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAA000000))
            .clickable(enabled = false) {},
        content = content
    )
}