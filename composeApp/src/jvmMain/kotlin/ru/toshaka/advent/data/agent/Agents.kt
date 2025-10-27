package ru.toshaka.advent.data.agent

fun DeepSeekChatAgent(config: AgentConfig<AiResponse>.() -> Unit): AgentConfig<AiResponse> {
    val defConfig: AgentConfig<AiResponse>.() -> Unit = {
        model = "deepseek-chat"
        baseUrl = "https://api.deepseek.com/chat/completions"
    }
    return AgentConfig<AiResponse>().apply(config).apply(defConfig)
}