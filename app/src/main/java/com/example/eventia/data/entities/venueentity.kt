package com.example.eventia.data.entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Tabla "venues": caché de lugares para el detalle sin conexión (RF-13).
 */
@Entity(tableName = "venues")
data class VenueEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val address: String?,
    val imageUrl: String?,
    val lat: Double?,
    val lng: Double?
)
