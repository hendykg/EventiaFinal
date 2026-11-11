package com.example.eventia.data.ApiConexion

import com.example.eventia.domain.model.AppError
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

/**
 * Clasificación central de errores HTTP y de red.
 * 401 -> SessionExpired, 409 -> NoAvailability, 404 -> NotFound (reglas de AGENTS.md).
 */
fun Throwable.toAppError(): AppError = when (this) {
    is HttpException -> when (code()) {
        401 -> AppError.SessionExpired
        404 -> AppError.NotFound
        409 -> AppError.NoAvailability
        in 500..599 -> AppError.Server(code())
        else -> AppError.Http(code())
    }
    is IOException -> AppError.Network
    else -> AppError.Unexpected(message ?: "Error inesperado")
}

/**
 * Envoltorio para llamadas a la API: mapea cualquier fallo a AppError
 * sin convertir la cancelación de corrutinas en un error ordinario.
 */
suspend fun <T> apiCall(block: suspend () -> T): T = try {
    block()
} catch (e: CancellationException) {
    throw e
} catch (e: Throwable) {
    throw e.toAppError()
}
