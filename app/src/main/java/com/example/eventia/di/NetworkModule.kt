package com.example.eventia.di

import com.example.eventia.data.ApiConexion.AuthInterceptor
import com.example.eventia.data.ApiConexion.EventiaApi
import com.example.eventia.data.ApiConexion.RetrofitClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /** Cliente con token Bearer (DataStore), logging y timeouts. */
    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            // Nivel BASIC: no registra encabezados ni cuerpos; el token nunca aparece en el log
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            )
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

    /**
     * La instancia de Retrofit la provee RetrofitClient (patrón del curso);
     * aquí solo se instala el cliente autenticado y se expone el servicio.
     */
    @Provides
    @Singleton
    fun provideEventiaApi(okHttpClient: OkHttpClient): EventiaApi {
        RetrofitClient.installClient(okHttpClient)
        return RetrofitClient.api
    }
}
