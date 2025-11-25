package ru.toshaka.advent.data.db.agent

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AgentRepository(
    private val agentDao: AgentDao,
) {

    suspend fun save(agent: AgentEntity) {
        agentDao.insert(agent)
    }

    fun getAllAsFlow(): Flow<List<AgentEntity>> =
        agentDao.getAllAsFlow().map { agents ->
            agents + AgentEntity(
                id = -100,
                name = "RAGer",
                systemPrompt = prompt,
                temperature = 0.2f,
                maxTokens = 8000
            )
        }

    suspend fun getAll(): List<AgentEntity> =
        agentDao.getAll()+ AgentEntity(
            id = -100,
            name = "RAGer",
            systemPrompt = prompt,
            temperature = 0.2f,
            maxTokens = 8000
        )

    suspend fun clear() {
        agentDao.clearAllMessages()
    }

    suspend fun clearHistory() {
        agentDao.clearHistory()
    }

    suspend fun update(item: AgentEntity) {
        agentDao.update(item)
    }
}

val prompt = """
   Ты — интеллектуальный агент с доступом к инструменту RAG через MCP. Все ответы должны быть строго в JSON формате:

1. **Всегда сначала проверяй RAG.** Даже если тебе кажется, что знаешь ответ, сначала вызывай инструмент "rag" с параметром "text" = запрос пользователя.
2. **Используй полученные из RAG чанки для построения ответа.** Если чанки пусты, тогда можешь признать, что данных нет.
3. **Никогда не отвечай «не знаю» или «уточните» до обращения к RAG.**
4. Формат ответа:
   {
     "type": "text",
     "content": "Твой текстовый ответ на вопрос пользователя на основе RAG"
   }
5. **Если RAG возвращает данные**, используй их полностью для ответа.
6. **Не добавляй никакие дополнительные комментарии** или пояснения вне JSON.
""".trimIndent()