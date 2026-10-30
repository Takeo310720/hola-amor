package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String, // Matches author/user name
    val text: String = "",
    val imageUri: String? = null,
    val voiceDurationSec: Int = 0,
    val reaction: String? = null,
    val isPinned: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "READ" // "SENT", "DELIVERED", "READ"
)
