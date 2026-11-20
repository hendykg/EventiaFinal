package com.example.eventia.domain.repository

import com.example.eventia.domain.model.User

/** Autenticación contra la API. Guarda la sesión al iniciar sesión o registrarse. */
interface AuthRepository {

    suspend fun login(email: String, password: String): User

    suspend fun register(name: String, email: String, password: String): User
}
