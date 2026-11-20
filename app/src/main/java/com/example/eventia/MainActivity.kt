package com.example.eventia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.eventia.ui.catalogo.CatalogoScreen
import com.example.eventia.ui.catalogo.CatalogoViewModel
import com.example.eventia.ui.cuenta.CuentaScreen
import com.example.eventia.ui.cuenta.CuentaViewModel
import com.example.eventia.ui.favoritos.FavoritosScreen
import com.example.eventia.ui.favoritos.FavoritosViewModel
import com.example.eventia.ui.mapa.MapScreen
import com.example.eventia.ui.mapa.MapViewModel
import com.example.eventia.ui.theme.EventiaTheme
import dagger.hilt.android.AndroidEntryPoint

/** Las cuatro pantallas de la barra inferior. */
private enum class Pantalla { CATALOGO, MAPA, FAVORITOS, CUENTA }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EventiaTheme {
                val catalogo: CatalogoViewModel = hiltViewModel()
                val mapa: MapViewModel = hiltViewModel()
                val favoritos: FavoritosViewModel = hiltViewModel()
                val cuenta: CuentaViewModel = hiltViewModel()

                var pantalla by rememberSaveable { mutableStateOf(Pantalla.CATALOGO) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            Pantalla.entries.forEach { destino ->
                                NavigationBarItem(
                                    selected = pantalla == destino,
                                    onClick = { pantalla = destino },
                                    icon = { Icon(destino.icono(), contentDescription = null) },
                                    label = { Text(destino.etiqueta()) }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    val contenido = Modifier.padding(innerPadding)
                    when (pantalla) {
                        Pantalla.CATALOGO -> CatalogoScreen(
                            viewModel = catalogo,
                            onIrACuenta = { pantalla = Pantalla.CUENTA },
                            modifier = contenido
                        )

                        Pantalla.MAPA -> MapScreen(
                            viewModel = mapa,
                            onEventClick = { pantalla = Pantalla.CATALOGO },
                            modifier = contenido
                        )

                        Pantalla.FAVORITOS -> FavoritosScreen(
                            viewModel = favoritos,
                            modifier = contenido
                        )

                        Pantalla.CUENTA -> CuentaScreen(
                            viewModel = cuenta,
                            modifier = contenido
                        )
                    }
                }
            }
        }
    }
}

private fun Pantalla.etiqueta(): String = when (this) {
    Pantalla.CATALOGO -> "Catálogo"
    Pantalla.MAPA -> "Mapa"
    Pantalla.FAVORITOS -> "Favoritos"
    Pantalla.CUENTA -> "Cuenta"
}

private fun Pantalla.icono(): ImageVector = when (this) {
    Pantalla.CATALOGO -> Icons.AutoMirrored.Filled.List
    Pantalla.MAPA -> Icons.Default.Place
    Pantalla.FAVORITOS -> Icons.Default.Favorite
    Pantalla.CUENTA -> Icons.Default.Person
}
