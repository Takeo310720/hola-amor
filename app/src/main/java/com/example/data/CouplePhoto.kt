package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "couple_photos")
data class CouplePhoto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val imageUri: String,
    val caption: String = "",
    val tag: String = "Nosotros", // "Cita", "Viaje", "Divertido", "Nosotros", "Recuerdo"
    val addedBy: String = "César",
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
