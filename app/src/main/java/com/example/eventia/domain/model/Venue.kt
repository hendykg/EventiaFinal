package com.example.eventia.domain.model

data class Venue(
    val id: Int,
    val name: String,
    val address: String?,
    val imageUrl: String?,
    val lat: Double?,
    val lng: Double?
)
