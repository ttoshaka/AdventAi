package ru.toshaka.advent.ui.view

import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import ru.toshaka.advent.ui.AgentData

@Composable
fun AddAgentScreen(
    onSave: (AgentData) -> Unit,
    onCancel: () -> Unit
) {
    var agentName by remember { mutableStateOf("") }
    var systemPrompt by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf("1.0") }
    var maxTokens by remember { mutableStateOf("1024") }

    BaseFormScreen(
        title = "Создать нового ИИ-агента",
        onCancel = onCancel,
        onSave = {
            val tempValue = temperature.toFloatOrNull()?.coerceIn(0f, 2f) ?: 1.0f
            val tokensValue = maxTokens.toIntOrNull() ?: 1024
            if (agentName.isNotBlank() && systemPrompt.isNotBlank()) {
                onSave(
                    AgentData(
                        name = agentName,
                        systemPrompt = systemPrompt,
                        temperature = tempValue,
                        maxTokens = tokensValue
                    )
                )
            }
        },
        saveEnabled = agentName.isNotBlank() && systemPrompt.isNotBlank()
    ) {
        FormField(
            label = "Имя агента",
            value = agentName,
            onValueChange = { agentName = it },
            placeholder = "Введите имя агента..."
        )

        FormField(
            label = "Системный промпт",
            value = systemPrompt,
            onValueChange = { systemPrompt = it },
            placeholder = "Введите системный промпт...",
            minHeight = 120.dp
        )

        FormField(
            label = "Температура (0.0–2.0)",
            value = temperature,
            onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d*$"))) temperature = it },
            placeholder = "Например, 1.0"
        )

        FormField(
            label = "Макс. токенов",
            value = maxTokens,
            onValueChange = { if (it.all(Char::isDigit)) maxTokens = it },
            placeholder = "Например, 1024"
        )
    }
}
