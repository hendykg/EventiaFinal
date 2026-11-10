package com.example.eventia.data.entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Tabla "events": caché del catálogo (RF-14) y favoritos (RF-04).
 * Denormalizada para servir el detalle básico sin conexión (RF-05).
 */
@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    val summary: String?,
    val description: String?,
    val startsAt: String,
    val categoryId: Int?,
    val categoryName: String?,
    val imageUrl: String?,
    val venueId: Int?,
    val venueName: String?,
    val venueAddress: String?,
    val venueLat: Double?,
    val venueLng: Double?,
    val capacity: Int?,
    val availableSpots: Int?,
    val isAttending: Boolean?,
    val isFavorite: Boolean = false,
    val isCached: Boolean = true,
    val cachedAt: Long = 0L
)
