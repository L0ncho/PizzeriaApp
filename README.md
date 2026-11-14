# PizzeriaApp

App Android (Kotlin + Jetpack Compose) de la pizzería. Integra la lógica desarrollada en las semanas 1 a 3 en el proyecto de consola [GestionPedidosPizzeria](https://github.com/L0ncho/GestionPedidosPizzeria) y, al iniciar, muestra el mensaje de bienvenida y la carta disponible leída desde los CSV.

## Requisitos

- Android Studio 2026.2.2.
- SDK de Android API 37 (Android 17).
- Emulador Pixel 4 con imagen API 37.
- Valores de `app/build.gradle.kts`:
  - `compileSdk`: 37
  - `targetSdk`: 37
  - `minSdk`: 23

## Cómo ejecutar

1. Abrir la carpeta `PizzeriaApp` en Android Studio.
2. Esperar a que termine la sincronización de Gradle.
3. Elegir el dispositivo Pixel 4 en la barra superior.
4. Presionar **Run**.

Resultado esperado: el mensaje "Bienvenidos a la mejor pizzería de la ciudad" y la carta con 6 pizzas, 4 de ellas marcadas con ★ (participan en promociones).

## Cómo correr las pruebas

En el panel **Android**, clic derecho sobre `cl.duoc.pmy2201.pizzeria (test)` → **Run**.

Son 62 pruebas unitarias que corren en el computador, sin emulador.

## Estructura

```
app/src/
├── main/
│   ├── java/cl/duoc/pmy2201/pizzeria/
│   │   ├── model/                 # Producto, pizzas, Pedido, Usuario, Cliente, Repartidor, cálculo de totales
│   │   ├── data/                  # LectorCsv y repositorios que leen los CSV
│   │   ├── service/               # Clientes, inventario, pedidos y asignación de repartidores
│   │   ├── util/                  # Validaciones, FormatoMoneda, AnalizadorClases
│   │   ├── ui/
│   │   │   ├── CartaDisponible.kt # Composable de la carta
│   │   │   └── theme/             # Tema de Compose (colores, tipografía)
│   │   └── MainActivity.kt        # Pantalla inicial: Greeting y carta
│   ├── assets/                    # inventario.csv, pizzas.csv, recetas.csv, repartidores.csv
│   └── res/values/strings.xml     # Textos de la pantalla
└── test/java/cl/duoc/pmy2201/pizzeria/  # Pruebas unitarias (model, data, service, util, ui)
```

## Requisitos de la semana 4

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

- **SDK mínimo API 23:** la versión de Android Studio usada no ofrece API 21 en la plantilla Empty Activity (la lista empieza en API 23); las instrucciones permiten "API 21 o superior" y se eligió la mínima disponible.
- **Core library desugaring:** `java.time.LocalDate` (fechas de la promoción) solo existe desde API 26.
- **CSV en assets:** `LectorCsv` recibe por parámetro cómo abrir cada archivo; la app usa assets y las pruebas usan el classpath (`src/main/assets` configurado como recursos de prueba).
- **FormatoMoneda:** usa `Locale.forLanguageTag("es-CL")` en lugar de `Locale.of`, que solo existe desde API 36.
- **Dependencias agregadas:** `kotlin-reflect` (anotación `@Promocionable` y análisis de clases) y `kotlin-test-junit` (pruebas).
- **Textos de la pantalla:** en `res/values/strings.xml`.
