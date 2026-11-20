package com.example.eventia.ui.catalogo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventia.domain.model.AppError
import com.example.eventia.domain.model.Category
import com.example.eventia.domain.model.Event
import com.example.eventia.domain.repository.EventRepository
import com.example.eventia.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CatalogoUiState {
    data object Loading : CatalogoUiState
    data class Content(val eventos: List<Event>) : CatalogoUiState
    data object Empty : CatalogoUiState
    data class Error(val mensaje: String) : CatalogoUiState
}

@HiltViewModel
class CatalogoViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CatalogoUiState>(CatalogoUiState.Loading)
    val uiState: StateFlow<CatalogoUiState> = _uiState.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _categoria = MutableStateFlow<Int?>(null)
    val categoria: StateFlow<Int?> = _categoria.asStateFlow()

    private val _categorias = MutableStateFlow<List<Category>>(emptyList())
    val categorias: StateFlow<List<Category>> = _categorias.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    val logueado: StateFlow<Boolean> = sessionRepository.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private var eventos = emptyList<Event>()
    private var busquedaJob: Job? = null

    init {
        viewModelScope.launch { cargarCategorias() }
        refrescar()
    }

    /** Descarga el catálogo completo y actualiza la caché. */
    fun refrescar() {
        viewModelScope.launch {
            _uiState.value = CatalogoUiState.Loading
            try {
                eventos = eventRepository.refreshCatalog()
                _uiState.value = mostrar(eventos)
            } catch (e: CancellationException) {
                throw e
            } catch (e: AppError) {
                _uiState.value = siHayDatosOEror(e)
            }
        }
    }

    fun onQueryChange(texto: String) {
        _query.value = texto
        programarBusqueda()
    }

    fun onCategoriaChange(id: Int?) {
        _categoria.value = id
        programarBusqueda()
    }

    /**
     * Debounce sencillo: cancela la búsqueda anterior y espera 300 ms
     * a que el usuario deje de escribir.
     */
    private fun programarBusqueda() {
        busquedaJob?.cancel()
        busquedaJob = viewModelScope.launch {
            delay(300)
            val texto = _query.value.trim().ifBlank { null }
            val idCategoria = _categoria.value
            if (texto == null && idCategoria == null) {
                refrescar()
            } else {
                buscar(texto, idCategoria)
            }
        }
    }

    private suspend fun buscar(texto: String?, idCategoria: Int?) {
        _uiState.value = CatalogoUiState.Loading
        try {
            eventos = eventRepository.searchEvents(texto, idCategoria)
            _uiState.value = mostrar(eventos)
        } catch (e: CancellationException) {
            throw e
        } catch (e: AppError) {
            _uiState.value = siHayDatosOEror(e)
        }
    }

    private suspend fun cargarCategorias() {
        try {
            _categorias.value = eventRepository.getCategories()
        } catch (e: CancellationException) {
            throw e
        } catch (e: AppError) {
            _mensaje.value = e.message
        }
    }

    fun alternarFavorito(evento: Event) {
        viewModelScope.launch {
            val nuevo = !evento.isFavorite
            eventRepository.setFavorite(evento.id, nuevo)
            reemplazar(evento.copy(isFavorite = nuevo))
        }
    }

    fun inscribirse(evento: Event) {
        viewModelScope.launch {
            try {
                reemplazar(eventRepository.attendEvent(evento.id))
                _mensaje.value = "Inscripción confirmada"
            } catch (e: CancellationException) {
                throw e
            } catch (e: AppError) {
                if (e is AppError.SessionExpired) sessionRepository.clearSession()
                _mensaje.value = e.message
            }
        }
    }

    fun cancelarInscripcion(evento: Event) {
        viewModelScope.launch {
            try {
                reemplazar(eventRepository.cancelAttendance(evento.id))
                _mensaje.value = "Inscripción cancelada"
            } catch (e: CancellationException) {
                throw e
            } catch (e: AppError) {
                if (e is AppError.SessionExpired) sessionRepository.clearSession()
                _mensaje.value = e.message
            }
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    private fun reemplazar(actualizado: Event) {
        eventos = eventos.map { if (it.id == actualizado.id) actualizado else it }
        _uiState.value = mostrar(eventos)
    }

    private fun mostrar(lista: List<Event>): CatalogoUiState =
        if (lista.isEmpty()) CatalogoUiState.Empty else CatalogoUiState.Content(lista)

    private fun siHayDatosOEror(e: AppError): CatalogoUiState =
        if (eventos.isNotEmpty()) CatalogoUiState.Content(eventos)
        else CatalogoUiState.Error(e.message ?: "Ocurrió un error")
}
