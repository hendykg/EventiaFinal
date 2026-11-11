package com.example.eventia.ui.mapa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventia.domain.model.AppError
import com.example.eventia.domain.model.Event
import com.example.eventia.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Estado de la pantalla del mapa: carga, contenido, vacío y error. */
sealed interface MapUiState {
    data object Loading : MapUiState
    data class Content(val events: List<Event>) : MapUiState
    data object Empty : MapUiState
    data class Error(val error: AppError) : MapUiState
}

@HiltViewModel
class MapViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Loading)
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    /** Evita mostrar "vacío" mientras la primera descarga aún no termina. */
    private var firstLoadDone = false

    init {
        observeCatalog()
        refresh()
    }

    /** La caché de Room alimenta la UI; se actualiza sola tras cada descarga (RF-14). */
    private fun observeCatalog() {
        viewModelScope.launch {
            eventRepository.observeCatalog().collect { events ->
                _uiState.update { current ->
                    when {
                        events.isNotEmpty() -> MapUiState.Content(events)
                        current is MapUiState.Error -> current
                        firstLoadDone -> MapUiState.Empty
                        else -> MapUiState.Loading
                    }
                }
            }
        }
    }

    /**
     * Descarga el catálogo desde la API (RF-06). Si ya hay datos visibles,
     * un fallo de red no los borra: se conserva el contenido.
     */
    fun refresh() {
        viewModelScope.launch {
            try {
                eventRepository.refreshCatalog()
            } catch (e: CancellationException) {
                throw e
            } catch (e: AppError) {
                _uiState.update { current ->
                    if (current is MapUiState.Content) current else MapUiState.Error(e)
                }
            } finally {
                firstLoadDone = true
            }
        }
    }
}
