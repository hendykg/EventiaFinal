# Eventia

Eventia es una aplicación Android desarrollada con Kotlin y Jetpack Compose para consultar eventos y lugares, e inscribirse a eventos. El proyecto está en desarrollo; este documento describe lo que existe hoy y distingue ese estado del flujo que se debe completar.

## Estructura actual del proyecto

La aplicación tiene un único módulo Gradle, `:app`. Los paquetes de Kotlin existentes son:

```text
app/src/main/java/com/example/eventia/
├── MainActivity.kt                     (host de la pantalla del mapa)
├── EventiaApplication.kt               (@HiltAndroidApp)
├── domain/
│   ├── model/                          (Category, Venue, Event, User, AppError)
│   └── repository/                      (interfaces: SessionRepository, EventRepository)
├── data/
│   ├── ApiConexion/
│   │   ├── EventiaApi.kt               (endpoints y DTO; BASE_URL en companion)
│   │   ├── AuthInterceptor.kt          (token Bearer centralizado)
│   │   └── ApiErrorMapper.kt           (clasificación 401/404/409/5xx/red)
│   ├── mappers/                        (DTO ↔ dominio ↔ entidad Room)
│   ├── repository/                     (SessionRepositoryImpl, EventRepositoryImpl)
│   ├── daos/                           (EventDao, VenueDao)
│   ├── entities/                       (EventEntity, VenueEntity)
│   ├── DataStoreManager.kt
│   └── EventiaDatabase.kt
├── di/                                 (DatabaseModule, NetworkModule, RepositoryModule)
├── util/Fechas.kt                      (fechas ISO a zona horaria local)
└── ui/
    ├── mapa/                           (MapViewModel, MapScreen)
    └── theme/
```

También existen recursos Android en `app/src/main/res`, el manifiesto en `app/src/main/AndroidManifest.xml` y las pruebas de ejemplo en `app/src/test` y `app/src/androidTest`.

### Responsabilidad del código existente

- **`MainActivity.kt`** inicia Compose, aplica `EventiaTheme` y muestra un `Scaffold` con el composable de ejemplo `Greeting`. Todavía no configura navegación ni pantallas de eventos o autenticación.
- **`data/ApiConexion/EventiaApi.kt`** declara las operaciones Retrofit para registro, inicio de sesión, perfil, categorías, eventos, lugares y asistencias. En el mismo archivo están sus modelos de solicitud/respuesta (DTO).
- **`data/ApiConexion/retrofitEventia.kt`** crea el cliente Retrofit y OkHttp. La URL base todavía es `https://tu-backend-eventia.com/`, por lo que debe cambiarse por la URL real antes de conectar el backend.
- **`data/DataStoreManager.kt`** ofrece funciones para guardar el token y datos del usuario, observar el token y borrar las preferencias de sesión.
- **`data/daos/eventdao.kt`** declara consultas Room para catálogo, favoritos e inserción de eventos.
- **`data/entities/`** contiene clases de datos relacionadas con eventos, categorías y lugares.
- **`ui/theme/`** contiene la configuración visual de Compose.

## Flujo implementado hoy

El flujo de interfaz que está conectado es:

```text
Android inicia MainActivity (@AndroidEntryPoint)
        ↓
EventiaTheme + Scaffold
        ↓
MapViewModel (@HiltViewModel, StateFlow<MapUiState>)
        ↓
EventRepository (interfaz) → EventRepositoryImpl
        ↓
Retrofit EventiaApi  →  caché Room  →  marcadores del mapa
```

`MapUiState` cubre carga, contenido, vacío y error. La búsqueda de eventos usa
`GET /events` con `q` y `category`; los favoritos y la caché viven en Room.

Las piezas de datos quedaron conectadas por Hilt:

```text
NetworkModule ──proporciona──> EventiaApi (OkHttp con AuthInterceptor y errores clasificados)

DatabaseModule ──proporciona──> EventiaDatabase ──> EventDao / VenueDao

RepositoryModule ──enlaza──> EventRepositoryImpl : EventRepository

DataStoreManager ──envuelve──> SessionRepositoryImpl : SessionRepository (token y sesión)
```

Aún faltan: navegación entre pantallas, catálogo con búsqueda y filtros,
favoritos con "Deshacer", cuenta y perfil, y la URL base real del backend.

## Flujo a completar al implementar una funcionalidad

Trabaja con la estructura que ya existe y añade piezas solo cuando las necesite el requisito:

1. La pantalla Compose recibe la acción del usuario y la delega a un ViewModel.
2. El ViewModel coordina el estado de pantalla y solicita la operación correspondiente a un repositorio.
3. El repositorio conecta `EventiaApi`, `DataStoreManager` o el DAO según el caso, y convierte las respuestas a datos que la UI pueda representar.
4. El resultado actualiza el estado de pantalla: carga, contenido o error.
5. La UI presenta el resultado; la navegación se decide en la capa de aplicación/presentación, no dentro de Retrofit ni del DAO.

Esta separación es la dirección para desarrollar el proyecto; no significa que las capas mencionadas ya estén creadas. No hace falta dividir `:app` en varios módulos Gradle para seguir este flujo.

## Reglas requeridas para errores HTTP

Estas reglas son requisitos para cuando se conecte el flujo de API con sesión y pantallas; no están implementadas todavía en la actividad actual.

### HTTP 401 — sesión vencida o no autorizada

- Borrar la sesión local mediante `DataStoreManager.borrarSesion()`.
- Dirigir al usuario a Login y quitar las pantallas protegidas del historial de navegación.
- Mantener la aplicación abierta; no finalizar la actividad ni el proceso.
- Evitar que varios 401 generen navegaciones repetidas.

La capa de datos debe comunicar el error de sesión a presentación/aplicación. No debe navegar por su cuenta ni confundir otros errores con un 401.

### HTTP 409 — evento sin cupos

- Informar al usuario que ya no hay cupos disponibles para el evento.
- No tratar la inscripción como exitosa ni marcar al usuario como asistente.
- Mantener la aplicación abierta y conservar el contexto de la pantalla.
- Representar el conflicto separadamente de errores de conexión u otros errores del servidor.

## Convenciones para los próximos cambios

- Conserva la estructura actual y sigue los nombres y paquetes existentes al extender una funcionalidad.
- Mantén la UI, el acceso a red y el almacenamiento local separados; introduce repositorios/ViewModels cuando se conecten esas capas.
- Representa los errores explícitamente. No ocultes excepciones ni conviertas fallos en resultados exitosos.
- Respeta la cancelación de corrutinas.
- Añade pruebas para los comportamientos implementados y sus casos de error. No describas como implementada una función que solo esté declarada o planificada.
- Consulta [AGENTS.md](./AGENTS.md) para las instrucciones del agente que acompañan al proyecto.

## Tecnologías configuradas

Kotlin, Android, Jetpack Compose/Material 3, Retrofit/Gson, OkHttp, DataStore, Room, Navigation Compose y Hilt están declarados en la configuración de `:app`. La presencia de una dependencia no implica que su flujo esté integrado en la aplicación.

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
