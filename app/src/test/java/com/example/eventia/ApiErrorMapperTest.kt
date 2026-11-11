package com.example.eventia

import com.example.eventia.data.ApiConexion.toAppError
import com.example.eventia.domain.model.AppError
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ApiErrorMapperTest {

    private fun httpError(code: Int): HttpException =
        HttpException(Response.error<Unit>(code, "".toResponseBody("application/json".toMediaType())))

    @Test
    fun `401 se clasifica como sesion expirada`() {
        assertTrue(httpError(401).toAppError() is AppError.SessionExpired)
    }

    @Test
    fun `409 se clasifica como sin cupos`() {
        assertTrue(httpError(409).toAppError() is AppError.NoAvailability)
    }

    @Test
    fun `404 se clasifica como recurso inexistente`() {
        assertTrue(httpError(404).toAppError() is AppError.NotFound)
    }

    @Test
    fun `500 se clasifica como error de servidor`() {
        assertTrue(httpError(500).toAppError() is AppError.Server)
    }

    @Test
    fun `IOException se clasifica como error de red`() {
        assertTrue(object : IOException("sin red") {}.toAppError() is AppError.Network)
    }
}
