package com.example.eventia.ui.mapa

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.eventia.domain.model.Event
import com.example.eventia.util.Fechas
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

// Posición inicial de la cámara mientras no hay marcadores (misma zona que los datos de prueba)
private val POSICION_INICIAL = LatLng(-17.3935, -66.1571)

/**
 * Pantalla del mapa de eventos (RF-07, RF-08).
 * Muestra marcadores solo para eventos con coordenadas válidas;
 * al seleccionar uno, abre una hoja inferior con su resumen.
 */
@Composable
fun MapScreen(
    state: MapUiState,
    onRetry: () -> Unit,
    onEventClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        is MapUiState.Loading -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        is MapUiState.Error -> Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = state.error.message ?: "Ocurrió un error",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text(text = "Reintentar")
            }
        }

        is MapUiState.Empty -> EventMap(
            events = emptyList(),
            onEventClick = onEventClick,
            modifier = modifier
        )

        is MapUiState.Content -> EventMap(
            events = state.events,
            onEventClick = onEventClick,
            modifier = modifier
        )
    }
}

@Composable
private fun EventMap(
    events: List<Event>,
    onEventClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // RF-07: los eventos sin coordenadas no generan marcadores ni errores
    val eventosConUbicacion = events.filter { it.hasValidLocation }
    var eventoSeleccionado by remember { mutableStateOf<Event?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(POSICION_INICIAL, 12f)
    }

    // Centra la cámara en los marcadores cuando llegan por primera vez
    LaunchedEffect(eventosConUbicacion) {
        if (eventosConUbicacion.isNotEmpty()) {
            val primero = eventosConUbicacion.first()
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(primero.venue.lat!!, primero.venue.lng!!),
                13f
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            contentDescription = "Mapa de eventos de Eventia"
        ) {
            eventosConUbicacion.forEach { event ->
                Marker(
                    state = rememberMarkerState(
                        key = "event-${event.id}",
                        position = LatLng(event.venue.lat!!, event.venue.lng!!)
                    ),
                    title = event.title,
                    snippet = event.venue.name,
                    onClick = {
                        eventoSeleccionado = event
                        true
                    }
                )
            }
        }

        if (eventosConUbicacion.isEmpty()) {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "Todavía no hay eventos con ubicación para mostrar en el mapa",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }

    // RF-08: resumen del evento en hoja inferior al seleccionar el marcador
    eventoSeleccionado?.let { event ->
        EventSummarySheet(
            event = event,
            onDismiss = { eventoSeleccionado = null },
            onOpenDetail = {
                onEventClick(event.id)
                eventoSeleccionado = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventSummarySheet(
    event: Event,
    onDismiss: () -> Unit,
    onOpenDetail: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
        ) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = event.venue.name,
                style = MaterialTheme.typography.bodyMedium
            )
            event.venue.address?.let { address ->
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = Fechas.fechaLocal(event.startsAt),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onOpenDetail,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Ver detalle")
            }
        }
    }
}
