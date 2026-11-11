package com.example.eventia.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


// Instancia del DataStore para Eventia
private val Context.dataStore by preferencesDataStore(name = "eventia_preferences")

class DataStoreManager(private val context: Context) {

    companion object {
        val CLAVE_TOKEN = stringPreferencesKey("jwt_token")
        val CLAVE_NOMBRE = stringPreferencesKey("user_name")
        val CLAVE_CORREO = stringPreferencesKey("user_email")
    }

    // Guardar los datos del usuario al hacer login (RF-11)
    suspend fun guardarUsuario(token: String, nombre: String, correo: String) {
        context.dataStore.edit { preferencias ->
            preferencias[CLAVE_TOKEN] = token
            preferencias[CLAVE_NOMBRE] = nombre
            preferencias[CLAVE_CORREO] = correo
        }
    }

    // Leer el token guardado para verificar la sesión
    val tokenGuardado: Flow<String?> = context.dataStore.data.map { preferencias ->
        preferencias[CLAVE_TOKEN]
    }

    // Leer el nombre del usuario guardado
    val nombreGuardado: Flow<String?> = context.dataStore.data.map { preferencias ->
        preferencias[CLAVE_NOMBRE]
    }

    // Leer el correo del usuario guardado
    val correoGuardado: Flow<String?> = context.dataStore.data.map { preferencias ->
        preferencias[CLAVE_CORREO]
    }

    // Cerrar sesión (borra todos los datos almacenados)
    suspend fun borrarSesion() {
        context.dataStore.edit { preferencias ->
            preferencias.clear()
        }
    }
}