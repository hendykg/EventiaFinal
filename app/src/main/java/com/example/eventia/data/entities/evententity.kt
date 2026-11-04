package com.example.eventia.data.entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "events")
data class evententity (
    @PrimaryKey
    val id: Int,
    val title: String,
    val description: String,
    val date: String,
    val time: String,
    val location: String,
    val image: String
)
