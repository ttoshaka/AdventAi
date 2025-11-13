package ru.toshaka.advent.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.toshaka.advent.ui.MainScreenState

@Composable
fun CreateChatScreen(
    availableAgents: List<MainScreenState.Agent>,
    onSave: (selectedAgents: List<Long>, String) -> Unit,
    onCancel: () -> Unit
) {
    var chatName by remember { mutableStateOf("") }
    val selectedAgents = remember { mutableStateMapOf<MainScreenState.Agent, Boolean>() }

    LaunchedEffect(availableAgents) {
        availableAgents.forEach { selectedAgents[it] = false }
    }

    BaseFormScreen(
        title = "Создать новый чат",
        onCancel = onCancel,
        onSave = {
            val selected = selectedAgents.filterValues { it }.keys.map { it.id }
            if (chatName.isNotBlank() && selected.isNotEmpty()) {
                onSave(selected, chatName)
            }
        },
        saveEnabled = chatName.isNotBlank() && selectedAgents.values.any { it }
    ) {
        FormField(
            label = "Имя чата",
            value = chatName,
            onValueChange = { chatName = it },
            placeholder = "Введите имя чата..."
        )

        Text("Выберите агентов", fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            availableAgents.forEach { agent ->
                val isSelected = selectedAgents[agent] ?: false
                AgentSelectorItem(agent, isSelected) {
                    selectedAgents[agent] = !isSelected
                }
            }
        }
    }
}

@Composable
private fun AgentSelectorItem(agent: MainScreenState.Agent, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = selected, onCheckedChange = { onClick() })
            Spacer(Modifier.width(8.dp))
            Column {
                Text(agent.name, fontWeight = FontWeight.Bold)
                Text(
                    "T=${agent.temperature}, Tokens=${agent.maxTokens}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    agent.systemPrompt.take(60) + if (agent.systemPrompt.length > 60) "…" else "",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
    }
}
