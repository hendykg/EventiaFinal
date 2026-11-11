package com.example.eventia.data.mappers

import com.example.eventia.data.ApiConexion.VenueResponse
import com.example.eventia.data.entities.VenueEntity
import com.example.eventia.domain.model.Venue

fun VenueResponse.toEntity(): VenueEntity = VenueEntity(
    id = id,
    name = name,
    address = address,
    imageUrl = imageUrl,
    lat = lat,
    lng = lng
)

fun VenueEntity.toDomain(): Venue = Venue(
    id = id,
    name = name,
    address = address,
    imageUrl = imageUrl,
    lat = lat,
    lng = lng
)
