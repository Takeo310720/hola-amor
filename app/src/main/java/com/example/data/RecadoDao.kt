package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecadoDao {
    @Query("SELECT * FROM recados ORDER BY isPinned DESC, timestamp DESC")
    fun getAllRecados(): Flow<List<RecadoItem>>

    @Query("SELECT * FROM recados WHERE isCompleted = 0 ORDER BY isPinned DESC, timestamp DESC")
    fun getPendingRecados(): Flow<List<RecadoItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecado(recado: RecadoItem): Long

    @Update
    suspend fun updateRecado(recado: RecadoItem)

    @Delete
    suspend fun deleteRecado(recado: RecadoItem)

    @Query("UPDATE recados SET isCompleted = :completed WHERE id = :id")
    suspend fun setCompleted(id: Long, completed: Boolean)

    @Query("UPDATE recados SET isPinned = NOT isPinned WHERE id = :id")
    suspend fun togglePin(id: Long)
}
