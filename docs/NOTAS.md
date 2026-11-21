# Notas del proyecto

Detalles de PizzeriaApp que no caben en el README: dependencias, decisiones, manejo de errores, reglas de negocio y el estado de la semana 4.

## Dependencias agregadas en la semana 5

Declaramos las dependencias en `gradle/libs.versions.toml` y las usamos como `libs.*` en cada `build.gradle.kts`. Es lo mismo que escribir `implementation "grupo:artefacto:versión"`, pero con las versiones en un solo archivo.

| Artefacto | Versión | Módulo | Para qué la usamos |
|-----------|---------|--------|--------------------|
| `androidx.lifecycle:lifecycle-runtime-ktx` | 2.11.0 | `:app` | Ciclo de vida de la Activity. Ya existía; solo unificamos su versión con el resto de lifecycle. |
| `androidx.lifecycle:lifecycle-viewmodel-ktx` | 2.11.0 | `:app` | `ViewModel` y `viewModelScope`. |
| `androidx.lifecycle:lifecycle-livedata-ktx` | 2.11.0 | `:app` | `LiveData` y `MutableLiveData`. |
| `androidx.lifecycle:lifecycle-viewmodel-compose` | 2.11.0 | `:app` | `viewModelFactory` e `initializer`, para crear el ViewModel con sus repositorios. |
| `androidx.compose.runtime:runtime-livedata` | BOM de Compose | `:app` | `observeAsState`, para observar los `LiveData` desde Compose. |
| `org.jetbrains.kotlinx:kotlinx-coroutines-android` | 1.11.0 | `:app` | Cargar los CSV en segundo plano con `Dispatchers.IO`. |
| `org.jetbrains.kotlinx:kotlinx-coroutines-test` | 1.11.0 | `:app` (pruebas) | `StandardTestDispatcher`, `runTest` y `Dispatchers.setMain`. |
| `androidx.arch.core:core-testing` | 2.2.0 | `:app` (pruebas) | `InstantTaskExecutorRule`, para que los `LiveData` se actualicen al instante en las pruebas. |
| Plugin `org.jetbrains.kotlin.jvm` | Versión de Kotlin (2.2.10) | `:data`, `:util` | Compilar estos módulos como librerías Kotlin JVM, sin Android. |

## Decisiones técnicas

- Separamos el proyecto en `:app`, `:data` y `:util`. `:data` y `:util` son librerías Kotlin JVM sin nada de Android. Así la lógica no depende de la pantalla, se puede reutilizar (por ejemplo, en el proyecto de consola) y sus pruebas corren rápido en el computador. Mantuvimos los nombres de paquete al mover los archivos.
- Usamos coroutines solo para la carga. Leer los CSV es entrada/salida y puede tardar, así que lo hacemos en `Dispatchers.IO` sin bloquear la pantalla. El dispatcher se recibe por parámetro para poder cambiarlo en las pruebas.
- Registrar un pedido trabaja con datos en memoria, así que lo hacemos en el hilo principal. Así tampoco puede haber dos registros al mismo tiempo sobre el mismo inventario.
- El ViewModel expone `LiveData` de solo lectura (con `MutableLiveData` privados) y Compose los observa con `observeAsState`. Cuando un valor cambia, la pantalla se vuelve a dibujar.
- El ViewModel sobrevive a la rotación, así que los clientes, pedidos e inventario del día no se pierden al girar la pantalla.
- Usamos `applicationContext.assets` para leer los CSV. El `LectorCsv` queda guardado dentro de los repositorios del ViewModel; si usáramos los assets de la Activity, quedaría retenida la Activity anterior después de una rotación.
- Usamos un tema propio, la paleta "albahaca y masa", sin color dinámico ni modo oscuro. Así la app y sus estados (disponible, no disponible, seleccionada) se ven igual en todos los equipos, sin depender del fondo de pantalla ni del modo del sistema.
- El formulario tiene dos pasos: primero validamos los datos del cliente y después se elige la pizza. Lo escrito, la pizza elegida y el paso actual se guardan con `rememberSaveable`, así que se conservan al girar la pantalla.

## Manejo de errores

Evitamos que un dato mal escrito o un CSV con problemas cierre la app. Para eso usamos tres formas:

- Validaciones que no lanzan excepciones. Las funciones de `util/.../util/Validaciones.kt` retornan `false` o `null` cuando el dato no sirve: `esNombreValido`, `esDireccionValida`, `esCorreoValido` y `esCantidadValida` retornan `false`, y `normalizarTelefono` retorna `null`. `RegistroPedidoService` junta todos los campos inválidos en un `Set<CampoPedido>` y la pantalla muestra el error bajo cada campo.
- Errores de carga que se acumulan. `LectorCsv` no lanza excepciones: si un archivo no existe o no se puede leer, retorna un `ResultadoLectura` con las filas que pudo leer y una lista de errores. Los repositorios CSV agregan a esa lista las filas inválidas, repetidas o sin receta, y siguen con el resto. El ViewModel junta los errores de productos, inventario y repartidores, y la pantalla los muestra en Avisos de carga.
- Resultados como sealed classes. Registrar un pedido no lanza excepciones; retorna un resultado que la pantalla revisa con `when`:
  - `ResultadoSolicitud` (`service/ResultadoSolicitud.kt`): `DatosInvalidos`, `Registrado`, `SinStock` y `SinCapacidad`.
  - `ResultadoRegistro` (`service/ResultadoRegistro.kt`): `Registrado`, `Rechazado` y `SinCapacidad`.
  - `ResultadoVerificacion` (`service/ResultadoVerificacion.kt`): `Disponible` e `Insuficiente`, con los ingredientes que faltan.

Las excepciones quedan solo para errores de programación, por ejemplo combinar pedidos de clientes distintos o descontar más stock del que hay. En esos casos usamos `require`.

## Reglas de negocio agregadas o ajustadas

Estas reglas no vienen de la pauta. Las agregamos para que el flujo de la pantalla tenga sentido.

- Igualdad de Cliente sin correo: dos clientes son el mismo si tienen el mismo nombre y el mismo correo. Si no tienen correo, se comparan por nombre y teléfono normalizado (`+56 9 3171 7552` y `931717552` son el mismo teléfono).
- Un cliente nuevo queda registrado aunque su pedido no se registre por falta de stock o de capacidad, y el mensaje lo dice.
- Mostramos "Cliente frecuente" cuando el cliente ya tenía al menos un pedido registrado en el día.
- Cada pedido guarda su propia dirección de entrega, y el cliente conserva la de su registro. Dos pedidos solo se pueden combinar (`+`) si tienen la misma dirección de entrega, sin distinguir mayúsculas ni espacios en los extremos.
- Una pizza está disponible si el inventario alcanza para preparar al menos 1 unidad. Lo recalculamos después de cada registro.

## Requisitos de la semana 4

Esta sección describe el estado del tag `semana-4`. Desde la semana 5 la pantalla y la estructura del proyecto cambiaron.

| Paso | Qué se hizo | Dónde verlo |
|------|-------------|-------------|
| Instalación de Android Studio (SDK, emulador y controladores) | Android Studio 2026.2.2 | Capturas de la entrega |
| Configuración inicial y SDK API 31 o superior | API 37 instalada | Capturas de la entrega |
| Proyecto Empty Activity, Kotlin, nombre PizzeriaApp, SDK mínimo | Proyecto creado desde la plantilla, `minSdk = 23` | `app/build.gradle.kts` |
| AVD Pixel 4 con imagen API 31 o superior | Imagen API 37 | Capturas de la entrega |
| Integración de la lógica en paquetes (Producto, Pedido, Usuario y el resto) | Capas del proyecto de consola copiadas y adaptadas | `model`, `data`, `service`, `util` |
| Greeting con "Bienvenidos a la mejor pizzería de la ciudad" | Greeting muestra el texto desde recursos | `MainActivity.kt` (Greeting) y `res/values/strings.xml` (`mensaje_bienvenida`) |
| Ejecución en el emulador con interfaz básica y funcionalidades importadas | Pantalla con bienvenida y carta leída desde los CSV | `MainActivity.kt` y `ui/CartaDisponible.kt` |

## Adaptaciones para Android

- SDK mínimo API 23: la versión de Android Studio que usamos no ofrece API 21 en la plantilla Empty Activity (la lista empieza en API 23). Las instrucciones permiten "API 21 o superior", así que elegimos la mínima disponible.
- Core library desugaring: lo activamos porque `java.time.LocalDate` (fechas de la promoción) solo existe desde API 26.
- CSV en assets: `LectorCsv` recibe por parámetro cómo abrir cada archivo. La app lee desde assets y las pruebas desde el classpath. Los recursos de prueba se configuran en `data/build.gradle.kts`, apuntando a `app/src/main/assets`.
- FormatoMoneda: usamos `Locale.forLanguageTag("es-CL")` en lugar de `Locale.of`, que solo existe desde API 36.
- Dependencias agregadas: `kotlin-reflect` (anotación `@Promocionable` y análisis de clases) y `kotlin-test-junit` (pruebas).
- Textos de la pantalla: están en `res/values/strings.xml`.
