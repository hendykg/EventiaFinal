package com.example.eventia.domain.model

data class Event(
    val id: Int,
    val title: String,
    val summary: String?,
    val description: String?,
    val startsAt: String,
    val category: Category,
    val imageUrl: String?,
    val venue: Venue,
    val capacity: Int,
    val availableSpots: Int,
    val isAttending: Boolean? = null,
    val isFavorite: Boolean = false
) {
    /** RF-07: solo genera marcador si el lugar tiene coordenadas válidas. */
    val hasValidLocation: Boolean
        get() = venue.lat != null && venue.lng != null
}
