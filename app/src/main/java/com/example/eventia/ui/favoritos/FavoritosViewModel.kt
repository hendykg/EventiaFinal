package com.example.eventia.ui.favoritos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventia.domain.model.Event
import com.example.eventia.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritosViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    /** Room emite solo: si quitas un favorito en otra pantalla, esto se actualiza. */
    val eventos: StateFlow<List<Event>> = eventRepository.observeFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun quitarFavorito(evento: Event) {
        viewModelScope.launch {
            eventRepository.setFavorite(evento.id, false)
        }
    }
}
