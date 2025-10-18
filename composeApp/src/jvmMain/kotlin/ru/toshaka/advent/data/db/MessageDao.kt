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
    fun getAll(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM MessageEntity")
    suspend fun getAllSync(): List<MessageEntity>

    @Query("DELETE FROM MessageEntity")
    suspend fun clearAllMessages()
}