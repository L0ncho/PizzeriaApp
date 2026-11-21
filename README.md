# PizzeriaApp

App Android de uso interno para gestionar los pedidos de una pizzería, hecha en Kotlin con Jetpack Compose y Material 3. Partió del proyecto de consola [GestionPedidosPizzeria](https://github.com/L0ncho/GestionPedidosPizzeria) de las semanas 1 a 3.
La forma B de las instrucciones habla de una veterinaria (mascotas, dueños, consultas); en este proyecto eso equivale a productos, clientes y pedidos de la pizzería.

## Requisitos y cómo ejecutar

- Android Studio 2026.2.2 con el SDK de API 37 y un AVD Pixel 4 con imagen API 37.
- `compileSdk` y `targetSdk` 37, `minSdk` 23.

Abrir la carpeta `PizzeriaApp`, esperar la sincronización de Gradle, elegir el Pixel 4 y presionar Run. Al abrir se ve la barra con la bienvenida, el resumen del día en 0, el formulario de datos del cliente y la lista de pedidos vacía.

## Cómo probar la app

Los pasos se siguen en orden, con la app recién abierta:

1. Escribir nombre `A`, teléfono `123` y dirección `Av 1`, y tocar Continuar: aparece un error bajo cada campo y el resumen sigue en 0 clientes y 0 pedidos.
2. Escribir `Ana Pérez`, `+56 9 3171 7552`, correo vacío y `Av. Siempre Viva 123`, continuar, tocar + una vez en Pepperoni y registrar: aparece "Pedido #1 registrado para Ana Pérez. Total: $11.990" y el resumen pasa a 1 cliente, 1 pedido y "Ana Pérez".
3. Repetir con Ana Pérez y el mismo teléfono, pero con dirección `Calle Oficina 789`, y sumar 2 Pepperoni y 2 Napolitana con +: como son más de 3 pizzas se aplica el 10% de descuento, y aparece "Pedido #2 registrado para Ana Pérez. Total: $41.364" con "Cliente frecuente", el pedido muestra "Entrega: Calle Oficina 789" y el resumen queda en 1 cliente, 2 pedidos y "Ana Pérez".
4. Con un cliente nuevo, `Pedro Soto`, `+56 9 8765 4321` y `Calle Falsa 456`, sumar 3 Cuatro Quesos con + y registrar: aparece "Cliente agregado, sin pedido: faltan ingredientes (Queso azul)" y el resumen queda en 2 clientes, 2 pedidos y "Pedro Soto".
5. Sin salir de esa pantalla, tocar − dos veces para dejar 1 Cuatro Quesos y registrar (pedido #3); luego registrar otra Cuatro Quesos para Pedro Soto (pedido #4, "Cliente frecuente"): el resumen queda en 2 clientes, 4 pedidos y "Pedro Soto", y la Cuatro Quesos pasa a "No disponible".

Los datos están en memoria: al cerrar la app el resumen vuelve a 0.

## Cómo correr las pruebas

Clic derecho en la carpeta `src/test/java` de cada módulo y Run. Todas corren sin emulador.
Son 105 pruebas: 15 en `:app`, 75 en `:data` y 15 en `:util`.

## Estructura

- `:app`: la interfaz (`MainActivity`, `PedidosViewModel`, pantallas Compose, textos y CSV). Usa `:data` y `:util`.
- `:data`: entidades, lectura de los CSV y servicios de clientes, inventario y pedidos. Usa `:util`.
- `:util`: validaciones, formato de moneda y teléfono, y reflection. No usa ningún otro módulo.

## Requisitos de la semana 5

| Paso | Qué se hizo | Dónde verlo |
|------|-------------|-------------|
| 1. Reestructuración y modularización | Separamos el proyecto en `:app`, `:data` y `:util`. Los errores se manejan sin cerrar la app (detalle en `docs/NOTAS.md`). | `settings.gradle.kts`, carpetas `app/`, `data/` y `util/` |
| 2. Configuración de Gradle | Incluimos los tres módulos y agregamos ViewModel, LiveData y Coroutines desde el catálogo de versiones. | `gradle/libs.versions.toml`, `app/build.gradle.kts`, `data/build.gradle.kts`, `util/build.gradle.kts` |
| 3. Pantalla inicial con Compose | Pantalla con resumen, formulario en dos pasos, lista de pedidos y avisos de carga. | `app/.../ui/PantallaInicio.kt` y los demás archivos de `ui/` |
| 4. Conexión de MainActivity | `MainActivity` obtiene el ViewModel, observa sus `LiveData` y le pasa los datos a la pantalla. | `app/.../MainActivity.kt`, `app/.../ui/PedidosViewModel.kt` |
| 5. Mensaje de resumen | Clientes, pedidos y último cliente, actualizados en cada registro. | `app/.../ui/ResumenDiaCard.kt`, `data/.../service/RegistroPedidoService.kt` |
| 6. Pruebas y validación | 105 pruebas unitarias, incluidas las del ViewModel y del registro de pedidos. | Carpetas `src/test` de cada módulo |
| 7. Entrega | Tag `semana-5` del repositorio. El ZIP incluye el documento Word de evidencias con capturas de la estructura modular, la pantalla con el resumen y la actualización de los datos. | Tag `semana-5`, documento Word en el ZIP |

## Limitaciones

- Los datos están solo en memoria.
- La promoción depende de la fecha del equipo.
- Las recetas se asocian a cada pizza por nombre exacto.
- Un cliente que una vez da correo y otra no queda registrado dos veces.
- Dos personas con el mismo nombre y teléfono, sin correo, cuentan como el mismo cliente.
- Los teléfonos internacionales solo aceptan códigos de país de 2 dígitos.
- El cliente conserva la dirección de su primer registro.

## Mejoras futuras

- Guardar los datos.
- Buscar clientes por teléfono con autocompletado.
- Direcciones guardadas por cliente, con opción de agregar una nueva.
- Navegación entre pantallas.
- Pantallas para inventario (stock y reabastecimiento), estados de los pedidos y repartidores.
- Estado Cancelado.
- Login y roles.
- GPS de repartidores.
- Bebidas e ingredientes adicionales.
- Tamaños de pizza.
- Modo oscuro.

Más detalles sobre decisiones y dependencias en docs/NOTAS.md
