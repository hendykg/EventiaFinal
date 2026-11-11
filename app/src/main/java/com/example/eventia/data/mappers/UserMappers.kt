package com.example.eventia.data.mappers

import com.example.eventia.data.ApiConexion.UserResponse
import com.example.eventia.domain.model.User

fun UserResponse.toDomain(): User = User(
    id = id,
    name = name,
    email = email
)
