package com.example.eventia.ui.componentes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.eventia.domain.model.Event
import com.example.eventia.util.Fechas

/**
 * Tarjeta reutilizable de un evento.
 * La misma se usa en Catálogo, Favoritos e Inscripciones.
 */
@Composable
fun EventoRow(
    evento: Event,
    modifier: Modifier = Modifier,
    mostrarInscripcion: Boolean = true,
   logueado: Boolean = false,
    onFavorito: () -> Unit,
    onInscribirse: () -> Unit = {},
    onCancelar: () -> Unit = {},
    onIrALogin: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = evento.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onFavorito) {
                    Icon(
                        imageVector = if (evento.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (evento.isFavorite) "Quitar de favoritos" else "Agregar a favoritos"
                    )
                }
            }

            Text(Fechas.fechaLocal(evento.startsAt), style = MaterialTheme.typography.bodySmall)
            Text(evento.venue.name, style = MaterialTheme.typography.bodyMedium)
            Text("Cupos disponibles: ${evento.availableSpots}", style = MaterialTheme.typography.bodySmall)

            if (mostrarInscripcion) {
                Spacer(Modifier.height(12.dp))
                when {
                    !logueado -> Button(onClick = onIrALogin, modifier = Modifier.fillMaxWidth()) {
                        Text("Iniciar sesión para inscribirme")
                    }

                    evento.isAttending == true -> OutlinedButton(onClick = onCancelar, modifier = Modifier.fillMaxWidth()) {
                        Text("Cancelar inscripción")
                    }

                    else -> Button(onClick = onInscribirse, modifier = Modifier.fillMaxWidth()) {
                        Text("Inscribirme")
                    }
                }
            }
        }
    }
}
