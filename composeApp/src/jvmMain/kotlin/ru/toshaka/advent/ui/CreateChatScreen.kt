package ru.toshaka.advent.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CreateChatScreen(
    availableAgents: List<MainScreenState.Agent>,
    onSave: (selectedAgents: List<Long>) -> Unit,
    onCancel: () -> Unit
) {
    var chatName by remember { mutableStateOf("") }
    val selectedAgents = remember { mutableStateMapOf<MainScreenState.Agent, Boolean>() }

    LaunchedEffect(availableAgents) {
        availableAgents.forEach { selectedAgents[it] = false }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Создать новый чат",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Имя чата
        Text(text = "Имя чата", fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        BasicTextField(
            value = chatName,
            onValueChange = { chatName = it },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.Black),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                .padding(12.dp),
            decorationBox = { inner ->
                if (chatName.isEmpty()) {
                    Text("Введите имя чата...", color = Color.Gray)
                }
                inner()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Выберите агентов", fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            availableAgents.forEach { agent ->
                val isSelected = selectedAgents[agent] ?: false

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedAgents[agent] = !isSelected }
                        .padding(vertical = 8.dp)
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { selectedAgents[agent] = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
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

                Divider(color = Color.LightGray.copy(alpha = 0.4f))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Кнопки
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
            ) {
                Text("Отмена")
            }
            Button(
                onClick = {
                    val selected = selectedAgents.filterValues { it }.keys.toList().map { it.id }
                    if (chatName.isNotBlank() && selected.isNotEmpty()) {
                        onSave(selected)
                    }
                },
                enabled = chatName.isNotBlank() && selectedAgents.values.any { it }
            ) {
                Text("Сохранить")
            }
        }
    }
}
