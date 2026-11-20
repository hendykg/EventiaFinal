package com.example.eventia.ui.cuenta

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.eventia.ui.componentes.Cargando
import com.example.eventia.util.Fechas

@Composable
fun CuentaScreen(
    viewModel: CuentaViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val logueado by viewModel.logueado.collectAsStateWithLifecycle()
    val nombre by viewModel.nombre.collectAsStateWithLifecycle()
    val correo by viewModel.correo.collectAsStateWithLifecycle()
    val registrando by viewModel.registrando.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensaje.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Mi cuenta", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        val error = (state as? CuentaUiState.Error)?.mensaje
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }
        if (mensaje != null) {
            Text(mensaje!!, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
        }

        if (!logueado) {
            if (registrando) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (registrando) viewModel.crearCuenta(name, email, password)
                    else viewModel.entrar(email, password)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (registrando) "Crear cuenta" else "Iniciar sesión")
            }

            TextButton(onClick = viewModel::cambiarModo) {
                Text(if (registrando) "Ya tengo cuenta" else "Crear una cuenta")
            }
        } else {
            Text("Hola, ${nombre ?: "usuario"}", style = MaterialTheme.typography.titleMedium)
            Text(correo ?: "", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))
            Button(onClick = viewModel::salir, modifier = Modifier.fillMaxWidth()) {
                Text("Cerrar sesión")
            }
            Spacer(Modifier.height(24.dp))
            Text("Mis inscripciones", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            when (val estado = state) {
                is CuentaUiState.Cargando -> Cargando(Modifier.height(120.dp))

                is CuentaUiState.Error -> Button(onClick = viewModel::cargarInscripciones) {
                    Text("Reintentar")
                }

                is CuentaUiState.SinSesion -> Text("Inicia sesión para ver tus inscripciones.")

                is CuentaUiState.ConSesion -> {
                    if (estado.inscripciones.isEmpty()) {
                        Text("Todavía no te inscribiste en ningún evento.")
                    } else {
                        estado.inscripciones.forEach { evento ->
                            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(evento.title, style = MaterialTheme.typography.titleSmall)
                                    Text(Fechas.fechaLocal(evento.startsAt), style = MaterialTheme.typography.bodySmall)
                                    Text(evento.venue.name, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
