This is a Kotlin Multiplatform project targeting Android, iOS.

* [/iosApp](./iosApp/iosApp) contains an iOS application. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./shared/src/jvmMain/kotlin)
    folder is the appropriate location.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :shared:testAndroidHostTest`
- iOS tests: `./gradlew :shared:iosSimulatorArm64Test`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…

# Conectividad REST

## Arquitectura de conexion

El flujo de productos conserva Clean Architecture + MVVM:

`ProductoScreen -> ProductoViewModel -> UseCase -> ProductoRepository -> ProductoRemoteDataSource -> Ktor -> EscuelaJS`

La respuesta vuelve por la ruta inversa:

`JSON -> ProductoDto -> mapper -> Producto -> ProductoUi -> UI`

La UI no llama a Ktor directamente. El ViewModel solo usa casos de uso, y la conversion entre DTO y dominio vive en `shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobil/data/mapper/ProductoMapper.kt`.

## API

API: Platzi Fake Store API / EscuelaJS

BASE_URL:

`https://api.escuelajs.co/api/v1/`

La configuracion central esta en:

`shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobil/data/remote/ApiConfig.kt`

Endpoints:

- `GET /products`
- `GET /products/{id}`
- `POST /products`
- `PUT /products/{id}`
- `DELETE /products/{id}`

## Ktor

El cliente HTTP multiplataforma se crea en `shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobil/data/remote/HttpClientFactory.kt`.

Configuracion activa:

- `expectSuccess = true`.
- `ContentNegotiation` con `kotlinx.serialization`.
- `Json { ignoreUnknownKeys = true, isLenient = true, encodeDefaults = true }`.
- `DefaultRequest` con `ApiConfig.BASE_URL` y `Content-Type: application/json`.
- `HttpTimeout`.
- `Logging` con `LogLevel.BODY` para desarrollo.
- Motor Android en `androidMain`.
- Motor Darwin en `iosMain`.

## Recurso consumido

El recurso principal es `Producto`, porque ya existia en la app como modelo de dominio y pantalla funcional.

Modelo de dominio:

- `id: Long`
- `nombre: String`
- `precio: Double`
- `stock: Int`

EscuelaJS no devuelve `stock`. El mapper usa `stock = 0` como valor local para mantener el dominio actual sin inventar un campo remoto.

## DTO utilizados

DTO de respuesta:

- `ProductoDto`
- `CategoriaDto`

DTO de envio:

- `ProductoRequestDto`

Estan en `shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobil/data/remote/dto/ProductoDto.kt`.

## Manejo de errores

`ProductoRepositorioRest` traduce errores tecnicos en mensajes entendibles:

- 404: producto no encontrado.
- 4xx: solicitud rechazada por la API.
- 5xx: error del servidor.
- Sin conexion: no hay conexion con el servicio o URL incorrecta.
- Timeout: la solicitud tardo demasiado.
- JSON incompatible: la respuesta no coincide con el DTO.

La aplicacion no debe cerrarse: el ViewModel recibe el `Result.failure` y actualiza `ProductoUiState.Fase.Error`.

## Ejecutar Android

Con Android SDK configurado:

```bash
./gradlew :androidApp:assembleDebug
```

## Prueba GET

1. Ejecutar la app.
2. Abrir la pantalla `Productos`.
3. Confirmar que se cargan productos desde `https://api.escuelajs.co/api/v1/products`.
4. Revisar logs `KTOR:` para ver metodo, URL, status y respuesta.

## Prueba Registrar

1. Abrir `Productos`.
2. Completar Nombre, Precio y Stock.
3. Pulsar `Registrar`.
4. Revisar logs `KTOR:` para confirmar `POST /products`.

## Probar errores

- 404: llamar temporalmente a `ObtenerProductoUseCase` con `999999` o preparar una accion de prueba contra `GET /products/999999`.
- Sin conexion: activar modo avion antes de abrir `Productos` o antes de reintentar.
- Timeout: cambiar temporalmente `ApiConfig.REQUEST_TIMEOUT_MILLIS` a `1`, ejecutar la prueba y restaurarlo a `15_000`.
- Error de serializacion: cambiar temporalmente `ApiConfig.IGNORE_UNKNOWN_KEYS` a `false`; luego restaurarlo a `true`.

## Evidencias

No se incluyen resultados observados ni capturas inventadas. Para el PDF final deben tomarse desde una ejecucion real:

- Registro de Ktor con peticion y respuesta.
- App en emulador Android.
- App en simulador iOS.
- Resultado de los cinco escenarios de prueba.
