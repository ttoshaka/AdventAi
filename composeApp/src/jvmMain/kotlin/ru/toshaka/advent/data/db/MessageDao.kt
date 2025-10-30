package ru.toshaka.advent.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Insert
    suspend fun insert(item: MessageEntity)

    @Query("SELECT * FROM MessageEntity")
    fun getAllAsFlow(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM MessageEntity")
    suspend fun getAll(): List<MessageEntity>

    @Query("DELETE FROM MessageEntity")
    suspend fun clearAllMessages()

    @Query("DELETE FROM MessageEntity WHERE history = 1")
    suspend fun clearHistory()
}