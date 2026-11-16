package com.example.eventia.domain.model

/**
 * Errores de la aplicación, diferenciados por origen.
 * La UI decide cómo presentarlos; 401 y 409 tienen manejo obligatorio (AGENTS.md).
 */
sealed class AppError(message: String) : Exception(message) {

    /** HTTP 401: token ausente, inválido o expirado. */
    data object SessionExpired : AppError("Tu sesión ha expirado. Inicia sesión de nuevo.")

    /** HTTP 409: sin cupos o ya inscrito. */
    data object NoAvailability : AppError("Ya no quedan cupos para este evento.")

    /** HTTP 404: recurso inexistente. */
    data object NotFound : AppError("Este evento ya no está disponible.")

    /** HTTP 422: datos inválidos (los campos se validan en el formulario). */
    data object Validation : AppError("Revisa los datos ingresados.")

    /** No se pudo conectar con el servidor o la respuesta tardó demasiado. */
    data object Network : AppError("No se pudo conectar con el servidor. Revisa tu conexión e intenta de nuevo.")

    /** HTTP 5xx. */
    data class Server(val code: Int) : AppError("Error del servidor ($code). Intenta más tarde.")

    /** Otro código HTTP. */
    data class Http(val code: Int) : AppError("Error inesperado ($code).")

    /** Cualquier otro fallo. */
    data class Unexpected(override val message: String) : AppError(message)
}
