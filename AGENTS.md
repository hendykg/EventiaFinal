# Instrucciones para agentes — Eventia

Antes de implementar una funcionalidad, revisa el estado real del código y el flujo de trabajo y la arquitectura recomendada en [README.md](./README.md). La arquitectura del README es el destino gradual del proyecto; no asumas que capas o funciones aún no implementadas ya existen.

## Reglas de desarrollo

- Mantén Kotlin y Jetpack Compose, y sigue los patrones y convenciones existentes en los archivos que modifiques.
- Separa presentación, dominio y datos según el README. La UI no debe acceder directamente a Retrofit, DataStore ni Room; los datos no deben navegar ni mostrar mensajes.
- Mantén DTO de API, entidades locales y modelos de dominio separados cuando corresponda. No agregues abstracciones o capas sin una necesidad funcional.
- Realiza cambios pequeños y relacionados con la solicitud. No renombres ni reestructures código ajeno al cambio.
- Representa los errores explícitamente; no silencies excepciones ni conviertas fallos en resultados exitosos.
- No registres ni expongas credenciales, tokens o datos sensibles.

## Requisitos obligatorios de respuestas HTTP

- **401**: limpiar la sesión local en DataStore y llevar al usuario a Login, limpiando la navegación protegida. La aplicación debe permanecer abierta.
- **409 por falta de cupos**: informar claramente que no quedan cupos para el evento; no confirmar la asistencia ni tratar la respuesta como éxito.
- Mantén el manejo HTTP centralizado y la navegación en la capa de aplicación/presentación. Conserva los demás errores como fallos explícitos y diferenciados.
- Respeta la cancelación de corrutinas y no la presentes como un error ordinario.

## Verificación

Ejecuta las pruebas y la compilación relevantes para el cambio. En Windows, usa `.\gradlew.bat :app:testDebugUnitTest` y `.\gradlew.bat :app:assembleDebug`. Actualiza el README cuando cambie el flujo o la arquitectura.

---

## Bitácora del agente

Qué se realiza con el flujo, en orden. El agente modifica el código directamente, un módulo por vez; cada fase termina con build + tests + commit y actualiza esta bitácora.

| # | Fase | Requisitos | Estado |
|---|------|-----------|--------|
| 0 | Infraestructura que compila (Gradle, Room, Hilt base, Manifest) | — | ✅ Commit `235f632` |
| 1 | Capa de datos mínima: dominio, mappers, repositorios, DI, interceptor | Técnica | ✅ |
| E-base | Inyección de Google Maps + pantalla del mapa | RF-07, RF-08 | ✅ |
| E-base.1 | Conexión al backend real (URL configurable, HTTP local, fix de Room en runtime) | Técnica | ✅ |
| 2 | Navegación Compose + barra inferior de 4 destinos | UX | ⏳ Pendiente |
| 3 | Módulo A · Catálogo | RF-01, 02, 03, 06 | ⏳ Pendiente |
| 4 | Módulo B · Lugares | RF-13 | ⏳ Pendiente |
| 5 | Módulo C · Favoritos y sin conexión | RF-04, 05, 14 | ⏳ Pendiente |
| 6 | Módulo D · Cuenta e inscripciones | RF-11, 12 | ⏳ Pendiente |
| 7 | Módulo E completo (filtro por categoría, RF-09, RF-10) | RF-07 a 10 | ⏳ Pendiente |
| 8 | README final, escenarios verificados, tag `v1.0` | Entregables | ⏳ Pendiente |

### Fase 0 — Infraestructura que compila

- Room unificado a `androidx.room3` 3.0.3 (runtime + compiler vía KSP), Kotlin 2.2.10 + KSP 2.3.4.
- Entidades `EventEntity` (caché + favoritos) y `VenueEntity`; `EventDao` y `VenueDao` coherentes.
- `EventiaDatabase`, `EventiaApplication` (@HiltAndroidApp), permisos `INTERNET`/`ACCESS_NETWORK_STATE`, `@AndroidEntryPoint` en `MainActivity`.

### Fase 1 + E-base — Capa de datos y mapa

- Modelos de dominio (`Category`, `Venue`, `Event`, `User`) y `AppError` (401 → SessionExpired, 409 → NoAvailability, 404, red, 5xx).
- Mappers DTO ↔ dominio ↔ entidad Room; la UI nunca ve DTO.
- Interfaces de repositorio en `domain/repository`; implementaciones en `data/repository` con Hilt (`RepositoryModule` con bindings).
- `AuthInterceptor` centraliza el token Bearer desde DataStore; `EventiaApi` quedó sin `@Header` dispersos.
- `RetrofitClient` es la única fuente de Retrofit (patrón del curso, `by lazy`); `NetworkModule` le instala el `OkHttpClient` de Hilt. La URL se lee de `local.properties` (`API_BASE_URL`) → `BuildConfig` y por defecto apunta a `http://10.0.2.2:8000/api/`.
- **Corrección de runtime**: faltaba `ksp(libs.androidx.room3.compiler)`, por lo que el APK compilaba pero crasheaba al abrir con `ClassNotFoundException: EventiaDatabase_Impl`. Agregado y verificado en dispositivo.
- **HTTP local**: `usesCleartextTraffic="true"` para `10.0.2.2`/`localhost`; `<queries>` con esquema `geo` para RF-09.
- Contrato Postman alineado: IDs numéricos; `POST`/`DELETE attendance` devuelven el evento actualizado (201/200); errores 422 con `fields`; coordenadas aceptadas en el lugar o en el evento.
- **Inyección de Google Maps**: la clave se lee de `local.properties` (`MAPS_API_KEY`) vía `manifestPlaceholders` en `app/build.gradle.kts` y llega al Manifest como `${MAPS_API_KEY}`; la clave nunca se sube a Git.
- `MapViewModel` con `StateFlow<MapUiState>` (carga/contenido/vacío/error) y `MapScreen` con `GoogleMap`: marcadores solo para eventos con coordenadas válidas (RF-07) y hoja inferior con resumen al seleccionar (RF-08).
- Tests unitarios de mappers y de clasificación de errores HTTP.

### Pendientes que bloquean ver eventos reales

1. **Levantar el backend** en el puerto 8000 (`/api/`). En este equipo no está el proyecto del servidor; hay que arrancarlo desde su carpeta o usar la URL pública de la plataforma.
2. **Elegir `API_BASE_URL` según el dispositivo** en `local.properties`:
   - Emulador: `http://10.0.2.2:8000/api/` (valor por defecto).
   - Celular físico por USB: `http://localhost:8000/api/` + `adb reverse tcp:8000 tcp:8000`.
3. **MAPS_API_KEY real**: sin ella el mapa se ve gris aunque lleguen los eventos (se crea en Google Cloud Console → "Maps SDK for Android").
