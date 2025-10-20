package ru.toshaka.advent.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

@Entity
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val author: String,
    val role: Roles,
) {
    enum class Roles {
        user, assistant
    }
}

class Converters {
    @TypeConverter
    fun fromRole(role: MessageEntity.Roles): String = role.name

    @TypeConverter
    fun toRole(role: String): MessageEntity.Roles = MessageEntity.Roles.valueOf(role)
}