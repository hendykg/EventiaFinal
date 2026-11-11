package com.example.eventia.data.mappers

import com.example.eventia.data.ApiConexion.CategoryResponse
import com.example.eventia.data.ApiConexion.EventResponse
import com.example.eventia.data.ApiConexion.VenueResponse
import com.example.eventia.data.entities.EventEntity
import com.example.eventia.domain.model.Category
import com.example.eventia.domain.model.Event
import com.example.eventia.domain.model.Venue

// DTO de red -> modelo de dominio
fun EventResponse.toDomain(isFavorite: Boolean = false): Event = Event(
    id = id,
    title = title,
    summary = summary,
    description = description,
    startsAt = startsAt,
    category = category.toDomain(),
    imageUrl = imageUrl,
    venue = venue.toDomain(),
    capacity = capacity,
    availableSpots = availableSpots,
    isAttending = isAttending,
    isFavorite = isFavorite
)

fun CategoryResponse.toDomain(): Category = Category(id = id, name = name)

fun VenueResponse.toDomain(): Venue = Venue(
    id = id,
    name = name,
    address = address,
    imageUrl = imageUrl,
    lat = lat,
    lng = lng
)

// DTO de red -> entidad Room (caché y favoritos)
fun EventResponse.toEntity(
    isFavorite: Boolean,
    isCached: Boolean,
    cachedAt: Long
): EventEntity = EventEntity(
    id = id,
    title = title,
    summary = summary,
    description = description,
    startsAt = startsAt,
    categoryId = category.id,
    categoryName = category.name,
    imageUrl = imageUrl,
    venueId = venue.id,
    venueName = venue.name,
    venueAddress = venue.address,
    venueLat = venue.lat,
    venueLng = venue.lng,
    capacity = capacity,
    availableSpots = availableSpots,
    isAttending = isAttending,
    isFavorite = isFavorite,
    isCached = isCached,
    cachedAt = cachedAt
)

// Entidad Room -> modelo de dominio
fun EventEntity.toDomain(): Event = Event(
    id = id,
    title = title,
    summary = summary,
    description = description,
    startsAt = startsAt,
    category = Category(id = categoryId ?: 0, name = categoryName ?: ""),
    imageUrl = imageUrl,
    venue = Venue(
        id = venueId ?: 0,
        name = venueName ?: "",
        address = venueAddress,
        imageUrl = null,
        lat = venueLat,
        lng = venueLng
    ),
    capacity = capacity ?: 0,
    availableSpots = availableSpots ?: 0,
    isAttending = isAttending,
    isFavorite = isFavorite
)
