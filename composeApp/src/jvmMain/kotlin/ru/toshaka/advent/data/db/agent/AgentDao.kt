package ru.toshaka.advent.data.db.agent

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AgentDao {
    @Insert
    suspend fun insert(item: AgentEntity)

    @Query("SELECT * FROM AgentEntity")
    fun getAllAsFlow(): Flow<List<AgentEntity>>

    @Query("SELECT * FROM AgentEntity")
    suspend fun getAll(): List<AgentEntity>

    @Query("DELETE FROM AgentEntity")
    suspend fun clearAllMessages()

    @Query("DELETE FROM AgentEntity")
    suspend fun clearHistory()
}