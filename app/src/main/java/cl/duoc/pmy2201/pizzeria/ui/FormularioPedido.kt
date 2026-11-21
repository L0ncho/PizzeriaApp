package cl.duoc.pmy2201.pizzeria.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.duoc.pmy2201.pizzeria.R
import cl.duoc.pmy2201.pizzeria.model.Producto
import cl.duoc.pmy2201.pizzeria.model.esPromocionable
import cl.duoc.pmy2201.pizzeria.service.CampoPedido
import cl.duoc.pmy2201.pizzeria.service.ResultadoSolicitud
import cl.duoc.pmy2201.pizzeria.service.SolicitudPedido
import cl.duoc.pmy2201.pizzeria.util.FormatoMoneda
import cl.duoc.pmy2201.pizzeria.util.RANGO_CANTIDAD_PIZZAS
import cl.duoc.pmy2201.pizzeria.util.formatearTelefono
import cl.duoc.pmy2201.pizzeria.util.normalizarTelefono

private const val PASO_DATOS_CLIENTE: Int = 1
private const val PASO_ELEGIR_PIZZA: Int = 2
private const val MARCA_PROMOCIONABLE: String = " ★"
private const val SIMBOLO_QUITAR: String = "−"
private const val SIMBOLO_AGREGAR: String = "+"
private const val OPACIDAD_NO_DISPONIBLE: Float = 0.4f
private val CAMPOS_CLIENTE: Set<CampoPedido> =
    setOf(CampoPedido.NOMBRE, CampoPedido.TELEFONO, CampoPedido.CORREO, CampoPedido.DIRECCION)
private val CAMPOS_PIZZAS: Set<CampoPedido> = setOf(CampoPedido.PRODUCTO, CampoPedido.CANTIDAD)

/**
 * Guarda las cantidades (nombre de pizza → cantidad) como una lista plana de nombre y cantidad,
 * para conservarlas con `rememberSaveable` al girar la pantalla.
 */
private val SaverCantidades: Saver<Map<String, Int>, Any> = listSaver(
    save = { cantidades: Map<String, Int> ->
        cantidades.flatMap { (nombrePizza: String, cantidad: Int) -> listOf<Any>(nombrePizza, cantidad) }
    },
    restore = { guardado: List<Any> ->
        guardado.chunked(2).associate { par: List<Any> -> (par[0] as String) to (par[1] as Int) }
    }
)

/**
 * Formulario de pedido en dos pasos:
 * 1. Datos del cliente. "Continuar" valida esos datos con [onValidarCliente] y solo avanza si son válidos.
 * 2. Elegir las pizzas: cada cuadro (en filas de 2) tiene botones para quitar y agregar una unidad. Una pizza
 *    no disponible según [disponibilidad] se ve atenuada y no se puede agregar, y el total del pedido no puede
 *    pasar de [RANGO_CANTIDAD_PIZZAS]. Bajo los cuadros van el total de pizzas, la leyenda, las notas y el
 *    registro con [onRegistrar].
 *
 * Los valores y el paso se guardan con `rememberSaveable` para conservarlos al girar la pantalla; las cantidades
 * se guardan por nombre de pizza porque [Producto] no se puede guardar directamente. Si una pizza elegida deja
 * de estar disponible, su cantidad vuelve a 0.
 *
 * Errores: se muestran los de "Continuar" y los de un [ResultadoSolicitud.DatosInvalidos]. El error de un campo
 * desaparece en cuanto el operador lo edita (o cambia alguna cantidad, para los errores de pizzas), sin esperar
 * el siguiente envío; para eso se recuerdan los campos editados desde el último envío, ya que un resultado igual
 * al anterior no vuelve a llegar como cambio.
 *
 * Resultados: un [ResultadoSolicitud.Registrado] nuevo limpia todo y vuelve al paso 1 (el id del último pedido
 * que limpió el formulario se guarda para no limpiar de nuevo lo escrito después si se recrea la pantalla);
 * SinStock y SinCapacidad mantienen el paso 2 con los datos; DatosInvalidos con algún campo del cliente vuelve
 * al paso 1 mostrando esos errores. El mensaje del resultado ([mensajeResultado]) se muestra bajo el botón del
 * paso visible, para que el aviso de pedido registrado se vea aunque el formulario haya vuelto al paso 1.
 */
@Composable
fun FormularioPedido(
    productos: List<Producto>,
    disponibilidad: Map<Producto, Boolean>,
    cargando: Boolean,
    ultimoResultado: ResultadoSolicitud?,
    onValidarCliente: (nombre: String, telefono: String, correo: String, direccion: String) -> Set<CampoPedido>,
    onRegistrar: (SolicitudPedido) -> Unit,
    modifier: Modifier = Modifier
) {
    var paso: Int by rememberSaveable { mutableStateOf(PASO_DATOS_CLIENTE) }
    var nombre: String by rememberSaveable { mutableStateOf("") }
    var telefono: String by rememberSaveable { mutableStateOf("") }
    var correo: String by rememberSaveable { mutableStateOf("") }
    var direccion: String by rememberSaveable { mutableStateOf("") }
    var cantidades: Map<String, Int> by rememberSaveable(stateSaver = SaverCantidades) {
        mutableStateOf(emptyMap<String, Int>())
    }
    var notas: String by rememberSaveable { mutableStateOf("") }
    var idPedidoLimpiado: Int? by rememberSaveable { mutableStateOf<Int?>(null) }
    var erroresContinuar: Set<CampoPedido> by rememberSaveable { mutableStateOf(emptySet<CampoPedido>()) }
    var camposEditados: Set<CampoPedido> by rememberSaveable { mutableStateOf(emptySet<CampoPedido>()) }

    val camposResultado: Set<CampoPedido> =
        (ultimoResultado as? ResultadoSolicitud.DatosInvalidos)?.campos ?: emptySet()
    val tieneError: (CampoPedido) -> Boolean = { campo: CampoPedido ->
        campo in erroresContinuar || (campo in camposResultado && campo !in camposEditados)
    }
    val marcarEditados: (Set<CampoPedido>) -> Unit = { campos: Set<CampoPedido> ->
        erroresContinuar = erroresContinuar - campos
        camposEditados = camposEditados + campos
    }
    val cambiarCantidad: (Producto, Int) -> Unit = { producto: Producto, nuevaCantidad: Int ->
        cantidades = if (nuevaCantidad <= 0) {
            cantidades - producto.nombre
        } else {
            cantidades + (producto.nombre to nuevaCantidad)
        }
        marcarEditados(CAMPOS_PIZZAS)
    }
    val totalPizzas: Int = cantidades.values.sum()
    val mensaje: MensajeOperador? = ultimoResultado?.let { resultado: ResultadoSolicitud -> mensajeResultado(resultado) }

    LaunchedEffect(ultimoResultado) {
        when (ultimoResultado) {
            is ResultadoSolicitud.Registrado -> if (ultimoResultado.pedido.idPedido != idPedidoLimpiado) {
                nombre = ""
                telefono = ""
                correo = ""
                direccion = ""
                cantidades = emptyMap()
                notas = ""
                erroresContinuar = emptySet()
                camposEditados = emptySet()
                paso = PASO_DATOS_CLIENTE
                idPedidoLimpiado = ultimoResultado.pedido.idPedido
            }
            is ResultadoSolicitud.DatosInvalidos ->
                if (ultimoResultado.campos.any { campo: CampoPedido -> campo in CAMPOS_CLIENTE && campo !in camposEditados }) {
                    paso = PASO_DATOS_CLIENTE
                }
            else -> Unit
        }
    }

    LaunchedEffect(disponibilidad) {
        // Mientras no se conoce la disponibilidad (por ejemplo, al recrear la pantalla) no se descarta nada.
        if (disponibilidad.isEmpty()) return@LaunchedEffect
        val nombresDisponibles: Set<String> = disponibilidad
            .filterValues { disponible: Boolean -> disponible }
            .keys
            .map { producto: Producto -> producto.nombre }
            .toSet()
        val vigentes: Map<String, Int> = cantidades.filterKeys { nombrePizza: String -> nombrePizza in nombresDisponibles }
        if (vigentes != cantidades) cantidades = vigentes
    }

    if (paso == PASO_DATOS_CLIENTE) {
        Card(modifier = modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = stringResource(R.string.titulo_datos_cliente), style = MaterialTheme.typography.titleMedium)
                CampoTexto(
                    valor = nombre,
                    alCambiar = { valor: String ->
                        nombre = valor
                        marcarEditados(setOf(CampoPedido.NOMBRE))
                    },
                    etiqueta = R.string.campo_nombre,
                    error = R.string.error_nombre,
                    conError = tieneError(CampoPedido.NOMBRE)
                )
                CampoTexto(
                    valor = telefono,
                    alCambiar = { valor: String ->
                        telefono = valor
                        marcarEditados(setOf(CampoPedido.TELEFONO))
                    },
                    etiqueta = R.string.campo_telefono,
                    error = R.string.error_telefono,
                    conError = tieneError(CampoPedido.TELEFONO),
                    tipoTeclado = KeyboardType.Phone
                )
                CampoTexto(
                    valor = correo,
                    alCambiar = { valor: String ->
                        correo = valor
                        marcarEditados(setOf(CampoPedido.CORREO))
                    },
                    etiqueta = R.string.campo_correo,
                    error = R.string.error_correo,
                    conError = tieneError(CampoPedido.CORREO),
                    tipoTeclado = KeyboardType.Email
                )
                CampoTexto(
                    valor = direccion,
                    alCambiar = { valor: String ->
                        direccion = valor
                        marcarEditados(setOf(CampoPedido.DIRECCION))
                    },
                    etiqueta = R.string.campo_direccion,
                    error = R.string.error_direccion,
                    conError = tieneError(CampoPedido.DIRECCION)
                )
                Button(
                    onClick = {
                        val invalidos: Set<CampoPedido> = onValidarCliente(nombre, telefono, correo, direccion)
                        erroresContinuar = invalidos
                        if (invalidos.isEmpty()) paso = PASO_ELEGIR_PIZZA
                    },
                    enabled = !cargando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.boton_continuar))
                }
                if (mensaje != null) TextoMensaje(mensaje = mensaje)
            }
        }
    } else {
        Card(modifier = modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = stringResource(R.string.titulo_elegir_pizza), style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.cliente_nombre, nombre.trim()),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = stringResource(
                                R.string.cliente_telefono,
                                normalizarTelefono(telefono)?.let { normalizado: String -> formatearTelefono(normalizado) } ?: telefono
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    TextButton(onClick = { paso = PASO_DATOS_CLIENTE }) {
                        Text(text = stringResource(R.string.boton_cambiar_datos))
                    }
                }

                productos.chunked(2).forEach { fila: List<Producto> ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        fila.forEach { producto: Producto ->
                            val disponible: Boolean = disponibilidad[producto] == true
                            val cantidad: Int = cantidades[producto.nombre] ?: 0
                            CuadroPizza(
                                producto = producto,
                                disponible = disponible,
                                cantidad = cantidad,
                                puedeAgregar = disponible && totalPizzas < RANGO_CANTIDAD_PIZZAS.last,
                                alQuitar = { cambiarCantidad(producto, cantidad - 1) },
                                alAgregar = { cambiarCantidad(producto, cantidad + 1) },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                        if (fila.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }

                Text(
                    text = stringResource(R.string.total_pizzas, totalPizzas),
                    style = MaterialTheme.typography.titleSmall
                )
                if (tieneError(CampoPedido.PRODUCTO)) {
                    Text(
                        text = stringResource(R.string.error_pizza),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (tieneError(CampoPedido.CANTIDAD)) {
                    Text(
                        text = stringResource(R.string.error_cantidad),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(text = stringResource(R.string.leyenda_promocionable), style = MaterialTheme.typography.bodySmall)

                CampoTexto(
                    valor = notas,
                    alCambiar = { valor: String -> notas = valor },
                    etiqueta = R.string.campo_notas,
                    error = null,
                    conError = false,
                    unaLinea = false
                )
                Button(
                    onClick = {
                        erroresContinuar = emptySet()
                        camposEditados = emptySet()
                        onRegistrar(
                            SolicitudPedido(
                                nombre = nombre,
                                telefono = telefono,
                                correo = correo,
                                direccion = direccion,
                                cantidades = productos
                                    .associateWith { producto: Producto -> cantidades[producto.nombre] ?: 0 }
                                    .filterValues { cantidad: Int -> cantidad > 0 },
                                notas = notas
                            )
                        )
                    },
                    enabled = !cargando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.boton_registrar))
                }
                if (mensaje != null) TextoMensaje(mensaje = mensaje)
            }
        }
    }
}

/**
 * Cuadro de una pizza con su nombre, precio (con " ★" si es promocionable), si está disponible y una fila
 * para quitar o agregar unidades. Si tiene [cantidad] mayor que 0 se destaca con borde y fondo del color
 * primario; si no está [disponible] se ve atenuado. "−" se deshabilita en 0 y "+" cuando no [puedeAgregar].
 */
@Composable
private fun CuadroPizza(
    producto: Producto,
    disponible: Boolean,
    cantidad: Int,
    puedeAgregar: Boolean,
    alQuitar: () -> Unit,
    alAgregar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val elegida: Boolean = cantidad > 0
    val fondo: Color =
        if (elegida) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val marca: String = if (esPromocionable(producto)) MARCA_PROMOCIONABLE else ""
    val descripcionQuitar: String = stringResource(R.string.quitar_pizza, producto.nombre)
    val descripcionAgregar: String = stringResource(R.string.agregar_pizza, producto.nombre)
    Card(
        border = if (elegida) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        colors = CardDefaults.cardColors(containerColor = fondo),
        modifier = modifier.alpha(if (disponible) 1f else OPACIDAD_NO_DISPONIBLE)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = producto.nombre, style = MaterialTheme.typography.titleSmall)
            Text(text = "${FormatoMoneda.formatear(producto.precio)}$marca", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = stringResource(if (disponible) R.string.disponible else R.string.no_disponible),
                color = if (disponible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelMedium
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = alQuitar,
                    enabled = cantidad > 0,
                    modifier = Modifier.semantics { contentDescription = descripcionQuitar }
                ) {
                    Text(text = SIMBOLO_QUITAR, style = MaterialTheme.typography.titleLarge)
                }
                Text(text = cantidad.toString(), style = MaterialTheme.typography.titleMedium)
                IconButton(
                    onClick = alAgregar,
                    enabled = puedeAgregar,
                    modifier = Modifier.semantics { contentDescription = descripcionAgregar }
                ) {
                    Text(text = SIMBOLO_AGREGAR, style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}

/**
 * Mensaje del último resultado, con "Cliente frecuente" en otra línea si aplica
 * y en color de error si el pedido no se registró.
 */
@Composable
private fun TextoMensaje(mensaje: MensajeOperador) {
    val colorMensaje: Color = if (mensaje.esError) MaterialTheme.colorScheme.error else Color.Unspecified
    Text(
        text = stringResource(mensaje.texto, *mensaje.argumentos.toTypedArray()),
        color = colorMensaje,
        style = MaterialTheme.typography.bodyLarge
    )
    if (mensaje.clienteFrecuente) {
        Text(
            text = stringResource(R.string.mensaje_cliente_frecuente),
            color = colorMensaje,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Campo de texto del formulario con su etiqueta y, si [conError] es true, el texto [error] bajo el campo.
 */
@Composable
private fun CampoTexto(
    valor: String,
    alCambiar: (String) -> Unit,
    @StringRes etiqueta: Int,
    @StringRes error: Int?,
    conError: Boolean,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    unaLinea: Boolean = true
) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(text = stringResource(etiqueta)) },
        isError = conError,
        supportingText = if (conError && error != null) {
            { Text(text = stringResource(error)) }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
        singleLine = unaLinea,
        modifier = Modifier.fillMaxWidth()
    )
}
