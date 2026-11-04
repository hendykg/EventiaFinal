package com.example.eventia.data.daos

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.example.eventia.data.entities.evententity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    // Obtener catálogo desde caché (RF-14)
    @Query("SELECT * FROM events WHERE isCached = 1")
    fun getCachedEvents(): Flow<List<evententity>>

    // Obtener solo favoritos (RF-04)
    @Query("SELECT * FROM events WHERE isFavorite = 1")
    fun getFavoriteEvents(): Flow<List<evententity>>

    // Guardar lista devuelta por la API REST
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<evententity>)

    // Marcar/Desmarcar como favorito (RF-04)
    @Query("UPDATE events SET isFavorite = :isFavorite WHERE id = :eventId")
    suspend fun updateFavoriteStatus(eventId: Int, isFavorite: Boolean)

    // Limpiar caché antigua al actualizar desde red
    @Query("DELETE FROM events WHERE isFavorite = 0")
    suspend fun clearCachedCatalog()
}