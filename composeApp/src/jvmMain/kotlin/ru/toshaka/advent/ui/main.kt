package ru.toshaka.advent.ui

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ru.toshaka.advent.data.agent.AgentsManager
import ru.toshaka.advent.data.agent.AiResponse
import ru.toshaka.advent.data.agent.DeepSeekChatAgent

fun main() = application {
    val agentsManagers = AgentsManager()
    val flow1 = agentsManagers.addAgent(
        DeepSeekChatAgent {
            name = "Default agent"
            systemPrompt =
                "Ты AI-ассистент. Тебе нужно написать 1 метод на языке Kotlin, который будет выполнять поставленную задачу. В ответ должен быть только код."
            outputFormats = listOf(AiResponse.KotlinCodeResponse::class)
            isReceiveUserMessage = true
        }
    )
    val flow2 = agentsManagers.addAgent(
        DeepSeekChatAgent {
            name = "Answered agent"
            systemPrompt =
                "Ты AI-ассистент. Твоя обязанность придумать и написать один тест к переданному тебе методу на языке Kotlin."
            inputFormats = AiResponse.KotlinCodeResponse::class
            outputFormats = listOf(AiResponse.TextResponse::class)
        }
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "AdventAi_3",
    ) {
        App(
            messageFlow = listOf(flow1, flow2),
            onSendMessageClick = agentsManagers::onUserMessage,
            onClearClick = agentsManagers::clear
        )
    }
}