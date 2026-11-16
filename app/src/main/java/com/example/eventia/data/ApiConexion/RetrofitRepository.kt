package com.example.eventia.data.ApiConexion

import com.example.eventia.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Única fuente de Retrofit de la aplicación (patrón del curso).
 * La instancia y el servicio se crean una sola vez (by lazy) y se reutilizan.
 *
 * [installClient] lo llama NetworkModule para que Retrofit use el OkHttpClient
 * de Hilt, que agrega Authorization: Bearer mediante [AuthInterceptor].
 */
object RetrofitRepository {

    /**
     * URL base de la API, configurada en local.properties (API_BASE_URL):
     * - Emulador de Android Studio: "http://10.0.2.2:8000/api/" (por defecto)
     *   (en el emulador, "localhost" es el emulador; 10.0.2.2 apunta a la PC).
     * - Celular físico por USB: "http://localhost:8000/api/"
     *   (ejecutando antes `adb reverse tcp:8000 tcp:8000`).
     * - Servidor en la nube: la URL pública terminada en "/api/".
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

    /**
     * Instala el cliente de Hilt antes del primer acceso a [api].
     * Se conserva el primero instalado para no crear varias conexiones.
     */
    fun installClient(client: OkHttpClient) {
        if (okHttpClient == null) {
            okHttpClient = client
        }
    }

    fun getInstance(): Retrofit = retrofit
}
