package com.example.eventia.data.repository

import com.example.eventia.data.ApiConexion.EventiaApi
import com.example.eventia.data.ApiConexion.LoginRequest
import com.example.eventia.data.ApiConexion.RegisterRequest
import com.example.eventia.data.ApiConexion.apiCall
import com.example.eventia.data.mappers.toDomain
import com.example.eventia.domain.model.User
import com.example.eventia.domain.repository.AuthRepository
import com.example.eventia.domain.repository.SessionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: EventiaApi,
    private val sessionRepository: SessionRepository
) : AuthRepository {

    override suspend fun login(email: String, password: String): User = apiCall {
        val respuesta = api.login(LoginRequest(email, password))
        guardarSesion(respuesta.token, respuesta.user.toDomain())
    }

    override suspend fun register(name: String, email: String, password: String): User = apiCall {
        val respuesta = api.register(RegisterRequest(name, email, password))
        guardarSesion(respuesta.token, respuesta.user.toDomain())
    }

    private suspend fun guardarSesion(token: String, user: User): User {
        sessionRepository.saveSession(token, user)
        return user
    }
}
