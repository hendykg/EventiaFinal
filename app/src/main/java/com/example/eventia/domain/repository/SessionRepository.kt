package com.example.eventia.domain.repository

import com.example.eventia.domain.model.User
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    val token: Flow<String?>
    val userName: Flow<String?>
    val userEmail: Flow<String?>
    val isLoggedIn: Flow<Boolean>
    suspend fun saveSession(token: String, user: User)
    suspend fun clearSession()
}
