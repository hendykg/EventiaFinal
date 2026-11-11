package com.example.eventia.data.repository

import com.example.eventia.data.DataStoreManager
import com.example.eventia.domain.model.User
import com.example.eventia.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : SessionRepository {

    override val token: Flow<String?> = dataStoreManager.tokenGuardado
    override val userName: Flow<String?> = dataStoreManager.nombreGuardado
    override val userEmail: Flow<String?> = dataStoreManager.correoGuardado

    override val isLoggedIn: Flow<Boolean> = dataStoreManager.tokenGuardado.map { token ->
        !token.isNullOrBlank()
    }

    override suspend fun saveSession(token: String, user: User) {
        dataStoreManager.guardarUsuario(token, user.name, user.email)
    }

    override suspend fun clearSession() {
        dataStoreManager.borrarSesion()
    }
}
