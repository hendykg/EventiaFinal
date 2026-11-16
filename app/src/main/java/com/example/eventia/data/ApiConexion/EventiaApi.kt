package com.example.eventia.data.ApiConexion

import retrofit2.http.*

interface EventiaApi {

    // AUTENTICACIÓN Y CUENTA
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @GET("me")
    suspend fun getProfile(): UserResponse

    // CATEGORÍAS Y CATÁLOGO DE EVENTOS
    @GET("categories")
    suspend fun getCategories(): List<CategoryResponse>

    @GET("events")
    suspend fun getEvents(
        @Query("q") query: String? = null,
        @Query("category") categoryId: Int? = null
    ): List<EventResponse>

    @GET("events/{id}")
    suspend fun getEventDetail(@Path("id") id: Int): EventResponse

    // LUGARES
    @GET("venues")
    suspend fun getVenues(): List<VenueResponse>

    @GET("venues/{id}")
    suspend fun getVenueDetail(@Path("id") id: Int): VenueResponse

    @GET("venues/{id}/events")
    suspend fun getVenueEvents(@Path("id") id: Int): List<EventResponse>

    // INSCRIPCIONES / ASISTENCIA
    // El encabezado Authorization: Bearer lo agrega AuthInterceptor de forma centralizada.
    // El servidor responde 201/200 con el evento actualizado (incluye isAttending).
    @POST("events/{id}/attendance")
    suspend fun attendEvent(@Path("id") id: Int): EventResponse

    @DELETE("events/{id}/attendance")
    suspend fun cancelAttendance(@Path("id") id: Int): EventResponse

    @GET("me/attendances")
    suspend fun getMyAttendances(): List<EventResponse>
}


// ==========================================
// DATA CLASSES (Modelos DTO para la API)
// ==========================================

// Auth y Usuario
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val user: UserResponse
)

data class UserResponse(
    val id: Int,
    val name: String,
    val email: String
)

// Categoría
data class CategoryResponse(
    val id: Int,
    val name: String
)

// Lugar (Venue)
data class VenueResponse(
    val id: Int,
    val name: String,
    val address: String?,
    val imageUrl: String?,
    val lat: Double?,
    val lng: Double?,
    val upcomingEventsCount: Int? = null
)

// Evento (Catálogo y Detalle)
data class EventResponse(
    val id: Int,
    val title: String,
    val summary: String?,
    val description: String?,       // Puede venir en el detalle
    val startsAt: String,
    val category: CategoryResponse,
    val imageUrl: String?,
    val venue: VenueResponse,
    val capacity: Int,
    val availableSpots: Int,
    // Algunas respuestas traen las coordenadas en el evento y no en el lugar.
    val lat: Double? = null,
    val lng: Double? = null,
    val isAttending: Boolean? = null // Incluido si hay sesión en el detalle (RF-12)
)
