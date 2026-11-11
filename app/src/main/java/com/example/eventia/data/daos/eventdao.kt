package com.example.eventia.data.daos

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.example.eventia.data.entities.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    // Caché del catálogo (RF-14)
    @Query("SELECT * FROM events WHERE isCached = 1 ORDER BY startsAt ASC")
    fun getCachedEvents(): Flow<List<EventEntity>>

    // Favoritos (RF-04)
    @Query("SELECT * FROM events WHERE isFavorite = 1 ORDER BY startsAt ASC")
    fun getFavoriteEvents(): Flow<List<EventEntity>>

    // Detalle por id, para favoritos sin conexión (RF-05)
    @Query("SELECT * FROM events WHERE id = :eventId")
    fun getEventById(eventId: Int): Flow<EventEntity?>

    // Guardar el catálogo descargado (RF-14)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)

    // Marcar o desmarcar favorito; no requiere conexión (RF-04, RF-05)
    @Query("UPDATE events SET isFavorite = :isFavorite WHERE id = :eventId")
    suspend fun updateFavoriteStatus(eventId: Int, isFavorite: Boolean)

    // Invalidar la caché anterior conservando favoritos (RF-14)
    @Query("UPDATE events SET isCached = 0 WHERE isFavorite = 0")
    suspend fun invalidateCachedCatalog()

    // Borrar definitivamente lo que ya no es favorito ni caché
    @Query("DELETE FROM events WHERE isFavorite = 0 AND isCached = 0")
    suspend fun deleteStaleEvents()

    // Marca de tiempo de la última caché del catálogo (RF-14)
    @Query("SELECT MAX(cachedAt) FROM events WHERE isCached = 1")
    fun getCachedTimestamp(): Flow<Long?>
}
