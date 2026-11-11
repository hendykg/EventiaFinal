package com.example.eventia.data.repository

import com.example.eventia.data.ApiConexion.EventiaApi
import com.example.eventia.data.ApiConexion.apiCall
import com.example.eventia.data.daos.EventDao
import com.example.eventia.data.mappers.toDomain
import com.example.eventia.data.mappers.toEntity
import com.example.eventia.domain.model.Event
import com.example.eventia.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val api: EventiaApi,
    private val eventDao: EventDao
) : EventRepository {

    override suspend fun refreshCatalog(): List<Event> = apiCall {
        val dtos = api.getEvents()
        val now = System.currentTimeMillis()
        val favoriteIds = eventDao.getFavoriteEvents().first().map { it.id }.toSet()

        // La caché nueva reemplaza a la vieja conservando los favoritos (RF-14)
        eventDao.invalidateCachedCatalog()
        eventDao.deleteStaleEvents()
        eventDao.insertEvents(
            dtos.map { dto ->
                dto.toEntity(
                    isFavorite = dto.id in favoriteIds,
                    isCached = true,
                    cachedAt = now
                )
            }
        )
        dtos.map { dto -> dto.toDomain(isFavorite = dto.id in favoriteIds) }
    }

    override suspend fun searchEvents(query: String?, categoryId: Int?): List<Event> = apiCall {
        val favoriteIds = eventDao.getFavoriteEvents().first().map { it.id }.toSet()
        api.getEvents(query, categoryId).map { dto ->
            dto.toDomain(isFavorite = dto.id in favoriteIds)
        }
    }

    override fun observeCatalog(): Flow<List<Event>> =
        eventDao.getCachedEvents().map { entities -> entities.map { it.toDomain() } }

    override fun observeCatalogTimestamp(): Flow<Long?> = eventDao.getCachedTimestamp()

    override fun observeFavorites(): Flow<List<Event>> =
        eventDao.getFavoriteEvents().map { entities -> entities.map { it.toDomain() } }

    override fun observeEvent(eventId: Int): Flow<Event?> =
        eventDao.getEventById(eventId).map { it?.toDomain() }

    override suspend fun fetchEventDetail(eventId: Int): Event = apiCall {
        val dto = api.getEventDetail(eventId)
        val existing = eventDao.getEventById(eventId).first()
        // Conserva el estado local del evento; solo el detalle trae descripción completa
        val entity = dto.toEntity(
            isFavorite = existing?.isFavorite ?: false,
            isCached = existing?.isCached ?: false,
            cachedAt = System.currentTimeMillis()
        )
        eventDao.insertEvents(listOf(entity))
        entity.toDomain()
    }

    override suspend fun setFavorite(eventId: Int, isFavorite: Boolean) {
        eventDao.updateFavoriteStatus(eventId, isFavorite)
    }
}
