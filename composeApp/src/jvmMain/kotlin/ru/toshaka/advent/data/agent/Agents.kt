package ru.toshaka.advent.data.agent

fun DeepSeekChatAgent(config: AgentConfig<AiResponse>.() -> Unit): AgentConfig<AiResponse> {
    val defConfig: AgentConfig<AiResponse>.() -> Unit = {
        model = "deepseek-chat"
        baseUrl = "https://api.deepseek.com/chat/completions"
    }
    return AgentConfig<AiResponse>().apply(config).apply(defConfig)
}

val Summarizer: AgentConfig<AiResponse> = DeepSeekChatAgent {
    systemPrompt = """
            Ты — система сжатия информации.
            Тебе передан полный диалог между пользователем и ИИ-ассистентом.
            Твоя задача — создать краткое, связное и информативное резюме диалога, сохранив ключевые вопросы пользователя, важные ответы ассистента и итоговые выводы.
            Игнорируй приветствия, уточнения и повторяющиеся фразы.
            Если обсуждалось несколько тем — отрази их все кратко, но раздельно.
            Формат вывода:
            Краткое описание цели диалога
            Основные вопросы и ответы (по пунктам)
            Итог / решение / следующий шаг (если есть)
        """.trimIndent()
}