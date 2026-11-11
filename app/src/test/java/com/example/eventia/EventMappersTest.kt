package com.example.eventia

import com.example.eventia.data.ApiConexion.CategoryResponse
import com.example.eventia.data.ApiConexion.EventResponse
import com.example.eventia.data.ApiConexion.VenueResponse
import com.example.eventia.data.mappers.toDomain
import com.example.eventia.data.mappers.toEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EventMappersTest {

    private val dto = EventResponse(
        id = 42,
        title = "Noche de jazz en la plaza",
        summary = "Clásicos del jazz con músicos invitados.",
        description = null,
        startsAt = "2026-11-14T20:00:00-04:00",
        category = CategoryResponse(id = 3, name = "Música"),
        imageUrl = null,
        venue = VenueResponse(
            id = 7,
            name = "Casa de la Cultura",
            address = "Av. Principal 123",
            imageUrl = null,
            lat = -17.3935,
            lng = null
        ),
        capacity = 50,
        availableSpots = 36,
        isAttending = null
    )

    @Test
    fun `dto a dominio conserva los campos`() {
        val event = dto.toDomain(isFavorite = true)

        assertEquals(42, event.id)
        assertEquals("Noche de jazz en la plaza", event.title)
        assertEquals("Música", event.category.name)
        assertEquals("Casa de la Cultura", event.venue.name)
        assertEquals(50, event.capacity)
        assertEquals(36, event.availableSpots)
        assertTrue(event.isFavorite)
    }

    @Test
    fun `evento sin lng no tiene ubicacion valida`() {
        val event = dto.toDomain()
        assertFalse(event.hasValidLocation)
    }

    @Test
    fun `dto a entidad y de vuelta conserva lo esencial`() {
        val entity = dto.toEntity(isFavorite = true, isCached = true, cachedAt = 1000L)
        val event = entity.toDomain()

        assertEquals(dto.id, event.id)
        assertEquals(dto.title, event.title)
        assertEquals(dto.category.name, event.category.name)
        assertEquals(dto.venue.name, event.venue.name)
        assertEquals(dto.venue.lat, event.venue.lat)
        assertEquals(dto.availableSpots, event.availableSpots)
        assertTrue(event.isFavorite)
    }
}
