package com.example.eventia.data.entities

import androidx.room3.PrimaryKey

data class categoriaentity (
    @PrimaryKey
    val categoryId: Int?,
    val categoryName: String?,
    val categoryDescription: String?,
    val categoryImage: String?,
    val isCached: Boolean = true,
    val isFavorite: Boolean = false,
)
