package ru.toshaka.advent.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AddAgentScreen(
    onSave: (AgentData) -> Unit,
    onCancel: () -> Unit
) {
    var agentName by remember { mutableStateOf("") }
    var systemPrompt by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf("1.0") }
    var maxTokens by remember { mutableStateOf("1024") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Создать нового ИИ-агента",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Имя агента
        Text("Имя агента", fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        BasicTextField(
            value = agentName,
            onValueChange = { agentName = it },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.Black),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                .padding(12.dp),
            decorationBox = { inner ->
                if (agentName.isEmpty()) {
                    Text("Введите имя агента...", color = Color.Gray)
                }
                inner()
            }
        )

        Spacer(Modifier.height(16.dp))

        // Системный промпт
        Text("Системный промпт", fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        BasicTextField(
            value = systemPrompt,
            onValueChange = { systemPrompt = it },
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.Black),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                .padding(12.dp),
            decorationBox = { inner ->
                if (systemPrompt.isEmpty()) {
                    Text("Введите системный промпт...", color = Color.Gray)
                }
                inner()
            }
        )

        Spacer(Modifier.height(16.dp))

        // Температура
        Text("Температура (0.0–2.0)", fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        BasicTextField(
            value = temperature,
            onValueChange = {
                if (it.matches(Regex("^\\d*\\.?\\d*\$"))) temperature = it
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.Black),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                .padding(12.dp),
            decorationBox = { inner ->
                if (temperature.isEmpty()) {
                    Text("Введите температуру (например, 1.0)", color = Color.Gray)
                }
                inner()
            }
        )

        Spacer(Modifier.height(16.dp))

        // Максимальный лимит токенов
        Text("Макс. токенов", fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        BasicTextField(
            value = maxTokens,
            onValueChange = {
                if (it.all { c -> c.isDigit() }) maxTokens = it
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.Black),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                .padding(12.dp),
            decorationBox = { inner ->
                if (maxTokens.isEmpty()) {
                    Text("Введите лимит токенов (например, 1024)", color = Color.Gray)
                }
                inner()
            }
        )

        Spacer(Modifier.height(24.dp))

        // Кнопки
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = onCancel,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.LightGray)
            ) {
                Text("Отмена")
            }

            Button(
                onClick = {
                    val tempValue = temperature.toFloatOrNull() ?: 1.0f
                    val maxTokensValue = maxTokens.toIntOrNull() ?: 1024
                    if (agentName.isNotBlank() && systemPrompt.isNotBlank()) {
                        onSave(
                            AgentData(
                                name = agentName,
                                systemPrompt = systemPrompt,
                                temperature = tempValue.coerceIn(0f, 2f),
                                maxTokens = maxTokensValue
                            )
                        )
                    }
                },
                enabled = agentName.isNotBlank() && systemPrompt.isNotBlank()
            ) {
                Text("Сохранить")
            }
        }
    }
}
