package com.example.eventia.ui.favoritos

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.eventia.ui.componentes.EventoRow
import com.example.eventia.ui.componentes.MensajeVacio

@Composable
fun FavoritosScreen(
    viewModel: FavoritosViewModel,
    modifier: Modifier = Modifier
) {
    val eventos by viewModel.eventos.collectAsStateWithLifecycle()

    val lista = eventos
    if (lista.isEmpty()) {
        MensajeVacio("Aún no guardaste eventos favoritos.", modifier)
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(lista, key = { it.id }) { evento ->
                EventoRow(
                    evento = evento,
                    mostrarInscripcion = false,
                    onFavorito = { viewModel.quitarFavorito(evento) }
                )
            }
        }
    }
}
