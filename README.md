# Eventia

Eventia es una aplicación Android desarrollada con Kotlin y Jetpack Compose para consultar eventos y lugares, e inscribirse a eventos. La app consume la API pública de Eventia y funciona en emulador y en celular físico.

## Estructura actual del proyecto

La aplicación tiene un único módulo Gradle, `:app`. Los paquetes de Kotlin son:

```text
app/src/main/java/com/example/eventia/
├── MainActivity.kt                     (barra inferior y navegación entre 4 pantallas)
├── EventiaApplication.kt               (@HiltAndroidApp)
├── domain/
│   ├── model/                          (Category, Venue, Event, User, AppError)
│   └── repository/                     (interfaces: SessionRepository, EventRepository, AuthRepository)
├── data/
│   ├── ApiConexion/
│   │   ├── EventiaApi.kt               (endpoints y DTO)
│   │   ├── RetrofitClient.kt           (única instancia de Retrofit; BASE_URL desde local.properties)
│   │   ├── AuthInterceptor.kt          (token Bearer centralizado)
│   │   └── ApiErrorMapper.kt           (clasificación 401/404/409/422/5xx/red)
│   ├── mappers/                        (DTO ↔ dominio ↔ entidad Room)
│   ├── repository/                     (SessionRepositoryImpl, EventRepositoryImpl, AuthRepositoryImpl)
│   ├── daos/                           (EventDao, VenueDao)
│   ├── entities/                       (EventEntity, VenueEntity)
│   ├── DataStoreManager.kt
│   └── EventiaDatabase.kt
├── di/                                 (DatabaseModule, NetworkModule, RepositoryModule)
├── util/Fechas.kt                      (fechas ISO a zona horaria local)
└── ui/
    ├── catalogo/                       (CatalogoViewModel, CatalogoScreen)
    ├── favoritos/                      (FavoritosViewModel, FavoritosScreen)
    ├── cuenta/                         (CuentaViewModel, CuentaScreen)
    ├── componentes/                    (EventoRow, Estados)
    ├── mapa/                           (MapViewModel, MapScreen)
    └── theme/
```

También existen recursos Android en `app/src/main/res`, el manifiesto en `app/src/main/AndroidManifest.xml` y las pruebas en `app/src/test` y `app/src/androidTest`.

### Responsabilidad del código existente

- **`MainActivity.kt`** inicia Compose, aplica `EventiaTheme`, muestra un `Scaffold` con barra inferior y cambia entre cuatro pantallas con un `enum Pantalla` (sin `NavController`).
- **`ui/catalogo/`** muestra el catálogo, la búsqueda por nombre con debounce y el filtro por categoría; también permite marcar favoritos e inscribirse/cancelar.
- **`ui/favoritos/`** observa el `Flow` de favoritos de Room, así que se actualiza solo cuando cambia el catálogo.
- **`ui/cuenta/`** permite iniciar sesión, crear una cuenta, cerrar sesión y ver "Mis inscripciones".
- **`ui/mapa/`** dibuja marcadores de los eventos con coordenadas y abre una hoja inferior al tocar uno.
- **`data/ApiConexion/EventiaApi.kt`** declara las operaciones Retrofit para registro, inicio de sesión, perfil, categorías, eventos, lugares y asistencias. En el mismo archivo están sus DTO.
- **`data/ApiConexion/RetrofitClient.kt`** es la única fuente de `Retrofit` (patrón del curso, `by lazy`). La URL base se configura en `local.properties` (`API_BASE_URL`); `NetworkModule` le instala el `OkHttpClient` de Hilt con el token.
- **`data/DataStoreManager.kt`** guarda el token y los datos del usuario, y los expone como `Flow`.
- **`data/daos/EventDao.kt`** declara consultas Room para catálogo, favoritos, inscripción y borrado de caché.
- **`data/entities/`** contiene `EventEntity` y `VenueEntity` (caché y favoritos).
- **`ui/theme/`** contiene la configuración visual de Compose.

## Flujo implementado hoy

La navegación y el flujo de datos son:

```text
MainActivity (@AndroidEntryPoint)
        ↓
Scaffold + NavigationBar (Catálogo · Mapa · Favoritos · Cuenta)
        ↓
Pantalla Compose  →  ViewModel (@HiltViewModel, StateFlow)
        ↓
Repositorio (interfaz) → implementación
        ↓
Retrofit EventiaApi  /  Room  /  DataStore
```

Cada pantalla tiene su ViewModel:

| Pantalla | ViewModel | Qué hace |
|----------|-----------|----------|
| Catálogo | `CatalogoViewModel` | `refreshCatalog()`, `searchEvents()`, categorías, favoritos, inscribirse y cancelar |
| Mapa | `MapViewModel` | carga eventos con coordenadas desde la API/caché y expone `MapUiState` |
| Favoritos | `FavoritosViewModel` | `observeFavorites()` (Flow de Room); quitar favorito |
| Cuenta | `CuentaViewModel` | login, registro, sesión, cierre y `getMyAttendances()` |

Detalles del flujo:

- **Catálogo**: al abrir descarga `GET /events` y guarda la caché en Room. La búsqueda espera 300 ms después de la última tecla (`Job.cancel()` + `delay(300)`) y llama `GET /events?q=...&category=...`.
- **Favoritos**: la estrella solo actualiza Room (`isFavorite`), sin red. La pestaña Favoritos observa esa tabla y se refresca sola.
- **Inscripción**: `POST /events/{id}/attend` requiere sesión. Refleja el resultado en Room (`isAttending` y cupos). `DELETE` cancela la inscripción.
- **Errores**: 401 limpia la sesión (`clearSession`) y avisa; 409 muestra "Ya no quedan cupos"; 422 muestra validación; el resto se muestra con botón "Reintentar" donde aplica.
- **Mapa**: descarga el catálogo al iniciar y centra la cámara en el primer evento con coordenadas. Sin `MAPS_API_KEY` los marcadores existen pero el mapa se ve gris.

Las piezas de datos están conectadas por Hilt:

```text
NetworkModule ──proporciona──> EventiaApi (OkHttp con AuthInterceptor y errores clasificados)

DatabaseModule ──proporciona──> EventiaDatabase ──> EventDao / VenueDao

RepositoryModule ──enlaza──> EventRepositoryImpl : EventRepository
                              AuthRepositoryImpl : AuthRepository

DataStoreManager ──envuelve──> SessionRepositoryImpl : SessionRepository (token y sesión)
```

`AuthRepository` se separó de `SessionRepository` para evitar un ciclo de Hilt:
`AuthInterceptor → SessionRepository` y `AuthRepositoryImpl → EventiaApi + SessionRepository`.

## Conectar el backend

La URL base se configura en `local.properties` (archivo ignorado por Git) y Gradle
la inyecta en `BuildConfig.API_BASE_URL`; no se escribe en el código.

```properties
# Servidor público (activo actualmente)
API_BASE_URL=https://eventia.jmacboy.dev/api/

# Emulador de Android Studio con backend local (10.0.2.2 = PC anfitriona)
#API_BASE_URL=http://10.0.2.2:8000/api/

# Celular físico por USB con backend local (requiere adb reverse)
#API_BASE_URL=http://localhost:8000/api/
```

Con la URL pública no hace falta `adb reverse` ni levantar Laravel: funciona en
emulador y en celular físico. Para volver al backend local, descomenta su línea.

Para un celular físico **con backend local**, antes de abrir la app:

```powershell
adb reverse tcp:8000 tcp:8000
```

Comprueba que el servidor responde desde la PC:

```powershell
Invoke-WebRequest http://localhost:8000/api/events -UseBasicParsing
```

La app usa HTTP local cuando apunta al backend local, por eso el manifiesto declara
`usesCleartextTraffic="true"` (con HTTPS está permitido igual). Si el servidor no
responde, la pantalla muestra el estado de error con "Reintentar".

## Mapa (Google Maps)

La clave se lee de `local.properties` (`MAPS_API_KEY`) y llega al manifiesto como
`${MAPS_API_KEY}`; nunca se sube a Git. Sin clave la app funciona, pero el mapa se
ve gris (los eventos y marcadores igual cargan). Para habilitarlo:

1. Crea una clave en Google Cloud Console con "Maps SDK for Android" habilitado.
2. Escribe `MAPS_API_KEY=tu_clave` en `local.properties`.
3. Recompila e instala la app.

## Flujo a seguir al implementar una funcionalidad

Trabaja con la estructura que ya existe y añade piezas solo cuando las necesite el requisito:

1. La pantalla Compose recibe la acción del usuario y la delega a un ViewModel.
2. El ViewModel coordina el estado de pantalla y solicita la operación correspondiente a un repositorio.
3. El repositorio conecta `EventiaApi`, `DataStoreManager` o el DAO según el caso, y convierte las respuestas a datos que la UI pueda representar.
4. El resultado actualiza el estado de pantalla: carga, contenido o error.
5. La UI presenta el resultado; la navegación se decide en la capa de aplicación/presentación, no dentro de Retrofit ni del DAO.

Esta separación es la dirección para desarrollar el proyecto. No hace falta dividir `:app` en varios módulos Gradle para seguir este flujo.

## Reglas requeridas para errores HTTP

`ApiErrorMapper` clasifica los errores y los ViewModels deciden qué mostrar.

### HTTP 401 — sesión vencida o no autorizada

- Borrar la sesión local con `SessionRepository.clearSession()`.
- La app permanece abierta; el usuario puede volver a iniciar sesión desde Cuenta.
- Evitar que varios 401 generen avisos repetidos.

La capa de datos comunica el error a presentación; no navega por su cuenta ni confunde otros errores con un 401.

### HTTP 409 — evento sin cupos

- Informar que ya no hay cupos disponibles para el evento.
- No tratar la inscripción como exitosa ni marcar al usuario como asistente.
- Mantener la app abierta y conservar el contexto de la pantalla.

## Convenciones para los próximos cambios

- Conserva la estructura actual y sigue los nombres y paquetes existentes al extender una funcionalidad.
- Mantén la UI, el acceso a red y el almacenamiento local separados.
- Representa los errores explícitamente. No ocultes excepciones ni conviertas fallos en resultados exitosos.
- Respeta la cancelación de corrutinas.
- Añade pruebas para los comportamientos implementados y sus casos de error. No describas como implementada una función que solo esté declarada o planificada.
- Consulta [AGENTS.md](./AGENTS.md) para las instrucciones del agente que acompañan al proyecto.

## Tecnologías configuradas

Kotlin, Android, Jetpack Compose/Material 3, Retrofit/Gson, OkHttp, DataStore, Room, Google Maps Compose y Hilt. La UI observa estados de ViewModel, la descarga pasa por Retrofit/OkHttp con el token de DataStore, la caché y los favoritos viven en Room y las dependencias se resuelven con Hilt.

## Compilar y probar

En Windows:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

En macOS/Linux:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

Configura el SDK de Android localmente para Gradle y no incluyas credenciales, tokens ni claves en el repositorio.
