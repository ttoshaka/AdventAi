package ru.toshaka.advent.data.db.chat

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

@Entity
data class ChatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val agents: List<Long>,
)

class Converters {
    @TypeConverter
    fun fromRole(agents: List<Long>): String = agents.joinToString(";")

    @TypeConverter
    fun toRole(agents: String): List<Long> = agents.split(";").map { it.toLong() }
}