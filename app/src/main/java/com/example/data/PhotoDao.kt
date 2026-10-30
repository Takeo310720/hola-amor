package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Query("SELECT * FROM couple_photos ORDER BY isFavorite DESC, timestamp DESC")
    fun getAllPhotos(): Flow<List<CouplePhoto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: CouplePhoto): Long

    @Update
    suspend fun updatePhoto(photo: CouplePhoto)

    @Delete
    suspend fun deletePhoto(photo: CouplePhoto)

    @Query("UPDATE couple_photos SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long)
}
