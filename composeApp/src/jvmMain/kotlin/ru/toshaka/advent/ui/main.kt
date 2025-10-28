package ru.toshaka.advent.ui

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.*
import ru.toshaka.advent.data.agent.AgentsManager
import ru.toshaka.advent.data.agent.AiResponse
import ru.toshaka.advent.data.agent.DeepSeekChatAgent
import ru.toshaka.advent.mcp.Client
import ru.toshaka.advent.mcp.Server

fun main() = application {
    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    scope.launch {
        val server = Server()
        server.launch()
    }
    scope.launch {
        delay(5_000)
        val client = Client()
        val tools = client.connect()
        println("Tools = $tools")
        val response = client.call(1, 2)
        println("Call = $response")
    }

    val agentsManagers = AgentsManager()
    val flow1 = agentsManagers.addAgent(
        DeepSeekChatAgent {
            name = "Optimizer agent"
            systemPrompt = """
                Ты AI-ассистент. Твоя задача сжать переданный тебе текст, оставив только необходимые для корректного ответа данные.
                """
            outputFormats = listOf(AiResponse.TextResponse::class)
            isReceiveUserMessage = true
        }
    )
    val flow2 = agentsManagers.addAgent(
        DeepSeekChatAgent {
            name = "Default agent"
            systemPrompt = """
                Ты AI-ассистент.
                """
            outputFormats = listOf(AiResponse.TextResponse::class)
            inputFormats = AiResponse.TextResponse::class
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