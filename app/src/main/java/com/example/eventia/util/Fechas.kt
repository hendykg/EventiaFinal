package com.example.eventia.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Formatea una fecha ISO-8601 (con zona horaria) en la zona horaria local del dispositivo.
 * Si el formato no se puede interpretar, devuelve el texto original.
 */
object Fechas {

    private val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
    private val formatter = SimpleDateFormat("d MMM yyyy · HH:mm", Locale.forLanguageTag("es-ES"))

    fun fechaLocal(iso: String): String = try {
        val date: Date? = parser.parse(iso)
        if (date != null) formatter.format(date) else iso
    } catch (_: Exception) {
        iso
    }
}
