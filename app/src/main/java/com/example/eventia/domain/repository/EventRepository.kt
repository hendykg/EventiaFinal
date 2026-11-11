package com.example.eventia.domain.repository

import com.example.eventia.domain.model.Event
import kotlinx.coroutines.flow.Flow

interface EventRepository {

    /** Descarga el catálogo completo de la API y actualiza la caché en Room (RF-14). */
    suspend fun refreshCatalog(): List<Event>

    /** Búsqueda por nombre y/o filtro por categoría contra la API (RF-03). */
    suspend fun searchEvents(query: String?, categoryId: Int?): List<Event>

    /** Catálogo en caché, observable y disponible sin conexión (RF-05, RF-14). */
    fun observeCatalog(): Flow<List<Event>>

    /** Marca de tiempo de la última caché exitosa (RF-14). */
    fun observeCatalogTimestamp(): Flow<Long?>

    /** Favoritos guardados en Room (RF-04). */
    fun observeFavorites(): Flow<List<Event>>

    /** Detalle básico por id, para favoritos sin conexión (RF-05). */
    fun observeEvent(eventId: Int): Flow<Event?>

    /** Detalle desde la API; con sesión incluye isAttending (RF-12). */
    suspend fun fetchEventDetail(eventId: Int): Event

    /** Marca o desmarca favorito; solo Room, no requiere conexión (RF-04, RF-05). */
    suspend fun setFavorite(eventId: Int, isFavorite: Boolean)
}
