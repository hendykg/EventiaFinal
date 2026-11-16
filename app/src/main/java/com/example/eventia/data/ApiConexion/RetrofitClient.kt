package com.example.eventia.data.ApiConexion

import com.example.eventia.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Única fuente de Retrofit de la aplicación (patrón del curso).
 * La instancia y el servicio se crean una sola vez (by lazy).
 *
 * [installClient] lo llama NetworkModule para que Retrofit use el OkHttpClient
 * de Hilt, que agrega Authorization: Bearer mediante [AuthInterceptor].
 * Sin ese cliente, las llamadas con sesión responderían 401.
 */
object RetrofitClient {

    /**
     * URL base configurada en local.properties (API_BASE_URL):
     * - Emulador: "http://10.0.2.2:8000/api/"
     * - Celular físico por USB: "http://localhost:8000/api/" + adb reverse
     * - Servidor en la nube: la URL pública terminada en "/api/"
     */
    val BASE_URL: String = BuildConfig.API_BASE_URL

    @Volatile
    private var okHttpClient: OkHttpClient? = null

    private val retrofit: Retrofit by lazy {
        val builder = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
        okHttpClient?.let { builder.client(it) }
        builder.build()
    }

    /** Servicio Retrofit listo para usar, creado una sola vez. */
    val api: EventiaApi by lazy {
        retrofit.create(EventiaApi::class.java)
    }

    /** Instala el cliente de Hilt (token + logging) antes del primer acceso a [api]. */
    fun installClient(client: OkHttpClient) {
        if (okHttpClient == null) {
            okHttpClient = client
        }
    }
}
