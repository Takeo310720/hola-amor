package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recados")
data class RecadoItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val details: String = "",
    val category: String = "Pendiente", // "Urgente", "Amor", "Compras", "Cita", "Pendiente"
    val author: String = "César", // Who wrote it
    val target: String = "Para los dos", // "Para ti", "Para mí", "Para los dos"
    val isCompleted: Boolean = false,
    val isPinned: Boolean = false,
    val dueDate: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
