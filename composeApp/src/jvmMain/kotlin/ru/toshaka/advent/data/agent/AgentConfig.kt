package ru.toshaka.advent.data.agent

import ru.toshaka.advent.data.AgentApi
import ru.toshaka.advent.data.model.ChatResponse
import java.util.*

class AgentConfig {

    var name: String = UUID.randomUUID().toString()

    var temperature: Float = 1f

    var model: String = ""

    var baseUrl: String = ""

    var key: String = ""

    var systemPrompt: String = ""

    var history: () -> List<Pair<String, String>> = { emptyList() }
}

class Agent(private val config: AgentConfig) {
    private val api = AgentApi(config)
    val name: String = config.name
    suspend operator fun invoke(message: String): ChatResponse {
        return api(message, config.history())
    }
}

fun Agent(config: AgentConfig.() -> Unit): Agent =
    Agent(AgentConfig().apply(config))