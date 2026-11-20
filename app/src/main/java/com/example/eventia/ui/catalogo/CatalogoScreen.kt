package com.example.eventia.ui.catalogo

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.eventia.ui.componentes.Cargando
import com.example.eventia.ui.componentes.EventoRow
import com.example.eventia.ui.componentes.MensajeError
import com.example.eventia.ui.componentes.MensajeVacio

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    viewModel: CatalogoViewModel,
    onIrACuenta: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val categoria by viewModel.categoria.collectAsStateWithLifecycle()
    val categorias by viewModel.categorias.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensaje.collectAsStateWithLifecycle()
    val logueado by viewModel.logueado.collectAsStateWithLifecycle()

    Column(modifier.fillMaxSize()) {

        OutlinedTextField(
            value = query,
            onValueChange = viewModel::onQueryChange,
            label = { Text("Buscar eventos") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            FilterChip(
                selected = categoria == null,
                onClick = { viewModel.onCategoriaChange(null) },
                label = { Text("Todas") }
            )
            categorias.forEach { cat ->
                Spacer(Modifier.width(8.dp))
                FilterChip(
                    selected = categoria == cat.id,
                    onClick = { viewModel.onCategoriaChange(cat.id) },
                    label = { Text(cat.name) }
                )
            }
        }

        mensaje?.let { texto ->
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(texto, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                TextButton(onClick = viewModel::limpiarMensaje) { Text("Cerrar") }
            }
        }

        when (val estado = state) {
            is CatalogoUiState.Loading -> Cargando()

            is CatalogoUiState.Empty -> MensajeVacio("No hay eventos para mostrar.")

            is CatalogoUiState.Error -> MensajeError(
                texto = estado.mensaje,
                onReintentar = viewModel::refrescar
            )

            is CatalogoUiState.Content -> {
                val lista = estado.eventos
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
                ) {
                    items(lista, key = { it.id }) { evento ->
                        EventoRow(
                            evento = evento,
                            logueado = logueado,
                            onFavorito = { viewModel.alternarFavorito(evento) },
                            onInscribirse = { viewModel.inscribirse(evento) },
                            onCancelar = { viewModel.cancelarInscripcion(evento) },
                            onIrALogin = onIrACuenta
                        )
                    }
                }
            }
        }
    }
}
