package ru.toshaka.advent.data.db.chat

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Insert
    suspend fun insert(item: ChatEntity): Long

    @Query("SELECT * FROM ChatEntity")
    fun getAllAsFlow(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM ChatEntity")
    suspend fun getAll(): List<ChatEntity>

    @Query("DELETE FROM ChatEntity")
    suspend fun clearAllMessages()

    @Query("DELETE FROM ChatEntity")
    suspend fun clearHistory()
}