package ru.toshaka.advent.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.toshaka.advent.data.db.agent.AgentDao
import ru.toshaka.advent.data.db.agent.AgentEntity
import ru.toshaka.advent.data.db.chat.ChatDao
import ru.toshaka.advent.data.db.chat.ChatEntity
import ru.toshaka.advent.data.db.chat.Converters
import ru.toshaka.advent.data.db.message.MessageDao
import ru.toshaka.advent.data.db.message.MessageEntity

@Database(
    entities = [
        MessageEntity::class,
        ChatEntity::class,
        AgentEntity::class
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getMessageDao(): MessageDao
    abstract fun getChatDao(): ChatDao
    abstract fun getAgentDao(): AgentDao
}