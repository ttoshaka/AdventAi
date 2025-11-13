package ru.toshaka.advent.data.db.agent

import kotlinx.coroutines.flow.Flow

class AgentRepository(
    private val agentDao: AgentDao,
) {

    suspend fun save(agent: AgentEntity) {
        agentDao.insert(agent)
    }

    fun getAllAsFlow(): Flow<List<AgentEntity>> =
        agentDao.getAllAsFlow()

    suspend fun getAll(): List<AgentEntity> =
        agentDao.getAll()

    suspend fun clear() {
        agentDao.clearAllMessages()
    }

    suspend fun clearHistory() {
        agentDao.clearHistory()
    }
}