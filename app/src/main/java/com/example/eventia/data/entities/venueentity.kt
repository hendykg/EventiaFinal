package com.example.eventia.data.entities

import androidx.room3.PrimaryKey

data class venueentity(
    @PrimaryKey
    val venueId: Int?,
    val venueName: String?,
    val venueAddress: String?,
    val venueCapacity: Int?,
    val venueImage: String?,
    val availableSpots: String?
)
