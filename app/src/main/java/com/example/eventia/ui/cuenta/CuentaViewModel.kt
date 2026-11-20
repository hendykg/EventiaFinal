package com.example.eventia.ui.cuenta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventia.domain.model.AppError
import com.example.eventia.domain.model.Event
import com.example.eventia.domain.repository.AuthRepository
import com.example.eventia.domain.repository.EventRepository
import com.example.eventia.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CuentaUiState {
    data object SinSesion : CuentaUiState
    data object Cargando : CuentaUiState
    data class ConSesion(val inscripciones: List<Event>) : CuentaUiState
    data class Error(val mensaje: String) : CuentaUiState
}

@HiltViewModel
class CuentaViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository,
    private val eventRepository: EventRepository
) : ViewModel() {

    val logueado: StateFlow<Boolean> = sessionRepository.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val nombre: StateFlow<String?> = sessionRepository.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val correo: StateFlow<String?> = sessionRepository.userEmail
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiState = MutableStateFlow<CuentaUiState>(CuentaUiState.SinSesion)
    val uiState: StateFlow<CuentaUiState> = _uiState.asStateFlow()

    private val _registrando = MutableStateFlow(false)
    val registrando: StateFlow<Boolean> = _registrando.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    init {
        viewModelScope.launch {
            logueado.collect { estaLogueado ->
                if (estaLogueado) cargarInscripciones()
                else _uiState.value = CuentaUiState.SinSesion
            }
        }
    }

    fun cambiarModo() {
        _registrando.value = !_registrando.value
        _mensaje.value = null
    }

    fun entrar(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = CuentaUiState.Cargando
            try {
                authRepository.login(email.trim(), password)
                _mensaje.value = "Sesión iniciada"
            } catch (e: CancellationException) {
                throw e
            } catch (e: AppError) {
                _uiState.value = CuentaUiState.Error(e.message ?: "No se pudo iniciar sesión")
            }
        }
    }

    fun crearCuenta(name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = CuentaUiState.Cargando
            try {
                authRepository.register(name.trim(), email.trim(), password)
                _mensaje.value = "Cuenta creada"
            } catch (e: CancellationException) {
                throw e
            } catch (e: AppError) {
                _uiState.value = CuentaUiState.Error(e.message ?: "No se pudo crear la cuenta")
            }
        }
    }

    fun salir() {
        viewModelScope.launch {
            sessionRepository.clearSession()
            _mensaje.value = "Sesión cerrada"
        }
    }

    fun cargarInscripciones() {
        viewModelScope.launch {
            _uiState.value = CuentaUiState.Cargando
            try {
                _uiState.value = CuentaUiState.ConSesion(eventRepository.getMyAttendances())
            } catch (e: CancellationException) {
                throw e
            } catch (e: AppError.SessionExpired) {
                sessionRepository.clearSession()
                _uiState.value = CuentaUiState.SinSesion
            } catch (e: AppError) {
                _uiState.value = CuentaUiState.Error(e.message ?: "No se pudieron cargar tus inscripciones")
            }
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }
}
