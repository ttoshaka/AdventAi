package ru.toshaka.advent.data.agent

fun DeepSeekChatAgent(config: AgentConfig.() -> Unit): Agent {
    val defConfig: AgentConfig.() -> Unit = {
        model = "deepseek-chat"
        baseUrl = "https://api.deepseek.com/chat/completions"
    }
    return Agent(AgentConfig().apply(config).apply(defConfig))
}