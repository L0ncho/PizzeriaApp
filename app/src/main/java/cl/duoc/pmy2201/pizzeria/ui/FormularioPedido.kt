package cl.duoc.pmy2201.pizzeria.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.duoc.pmy2201.pizzeria.R
import cl.duoc.pmy2201.pizzeria.model.Producto
import cl.duoc.pmy2201.pizzeria.service.CampoPedido
import cl.duoc.pmy2201.pizzeria.service.ResultadoSolicitud
import cl.duoc.pmy2201.pizzeria.service.SolicitudPedido

/**
 * Formulario para registrar un pedido: datos del cliente, pizza, cantidad y notas.
 *
 * Cada campo marca su error si su [CampoPedido] está entre los campos inválidos de [ultimoResultado].
 * Bajo el botón se muestra el mensaje de [mensajeResultado], en color de error si el pedido no se registró.
 * Los valores se guardan con `rememberSaveable` para conservarlos al girar la pantalla; la pizza se guarda
 * por nombre porque [Producto] no se puede guardar directamente.
 * Cuando llega un [ResultadoSolicitud.Registrado] nuevo se limpian todos los campos; con cualquier otro
 * resultado los datos se mantienen para que el operador los corrija. El id del último pedido que limpió
 * el formulario también se guarda, para no limpiar de nuevo lo escrito después si se recrea la pantalla.
 *
 * @param onRegistrar Se llama con la [SolicitudPedido] armada al pulsar el botón, deshabilitado mientras [cargando].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioPedido(
    productos: List<Producto>,
    cargando: Boolean,
    ultimoResultado: ResultadoSolicitud?,
    onRegistrar: (SolicitudPedido) -> Unit,
    modifier: Modifier = Modifier
) {
    var nombre: String by rememberSaveable { mutableStateOf("") }
    var telefono: String by rememberSaveable { mutableStateOf("") }
    var correo: String by rememberSaveable { mutableStateOf("") }
    var direccion: String by rememberSaveable { mutableStateOf("") }
    var nombreProducto: String? by rememberSaveable { mutableStateOf<String?>(null) }
    var cantidad: String by rememberSaveable { mutableStateOf("") }
    var notas: String by rememberSaveable { mutableStateOf("") }
    var idPedidoLimpiado: Int? by rememberSaveable { mutableStateOf<Int?>(null) }
    var menuExpandido: Boolean by remember { mutableStateOf(false) }

    val productoSeleccionado: Producto? = productos.find { producto: Producto -> producto.nombre == nombreProducto }
    val camposInvalidos: Set<CampoPedido> =
        (ultimoResultado as? ResultadoSolicitud.DatosInvalidos)?.campos ?: emptySet()
    val mensaje: MensajeOperador? = ultimoResultado?.let { resultado: ResultadoSolicitud -> mensajeResultado(resultado) }

    LaunchedEffect(ultimoResultado) {
        if (ultimoResultado is ResultadoSolicitud.Registrado && ultimoResultado.pedido.idPedido != idPedidoLimpiado) {
            nombre = ""
            telefono = ""
            correo = ""
            direccion = ""
            nombreProducto = null
            cantidad = ""
            notas = ""
            idPedidoLimpiado = ultimoResultado.pedido.idPedido
        }
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = stringResource(R.string.titulo_formulario), style = MaterialTheme.typography.titleMedium)

            CampoTexto(
                valor = nombre,
                alCambiar = { valor: String -> nombre = valor },
                etiqueta = R.string.campo_nombre,
                error = R.string.error_nombre,
                conError = CampoPedido.NOMBRE in camposInvalidos
            )
            CampoTexto(
                valor = telefono,
                alCambiar = { valor: String -> telefono = valor },
                etiqueta = R.string.campo_telefono,
                error = R.string.error_telefono,
                conError = CampoPedido.TELEFONO in camposInvalidos,
                tipoTeclado = KeyboardType.Phone
            )
            CampoTexto(
                valor = correo,
                alCambiar = { valor: String -> correo = valor },
                etiqueta = R.string.campo_correo,
                error = R.string.error_correo,
                conError = CampoPedido.CORREO in camposInvalidos,
                tipoTeclado = KeyboardType.Email
            )
            CampoTexto(
                valor = direccion,
                alCambiar = { valor: String -> direccion = valor },
                etiqueta = R.string.campo_direccion,
                error = R.string.error_direccion,
                conError = CampoPedido.DIRECCION in camposInvalidos
            )

            val pizzaConError: Boolean = CampoPedido.PRODUCTO in camposInvalidos
            ExposedDropdownMenuBox(
                expanded = menuExpandido,
                onExpandedChange = { expandido: Boolean -> menuExpandido = expandido }
            ) {
                OutlinedTextField(
                    value = productoSeleccionado?.let { producto: Producto -> textoProductoCarta(producto) } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(text = stringResource(R.string.campo_pizza)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpandido) },
                    isError = pizzaConError,
                    supportingText = if (pizzaConError) {
                        { Text(text = stringResource(R.string.error_pizza)) }
                    } else {
                        null
                    },
                    singleLine = true,
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = menuExpandido,
                    onDismissRequest = { menuExpandido = false }
                ) {
                    productos.forEach { producto: Producto ->
                        DropdownMenuItem(
                            text = { Text(text = textoProductoCarta(producto)) },
                            onClick = {
                                nombreProducto = producto.nombre
                                menuExpandido = false
                            }
                        )
                    }
                }
            }

            CampoTexto(
                valor = cantidad,
                alCambiar = { valor: String -> cantidad = valor },
                etiqueta = R.string.campo_cantidad,
                error = R.string.error_cantidad,
                conError = CampoPedido.CANTIDAD in camposInvalidos,
                tipoTeclado = KeyboardType.Number
            )
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
                    onRegistrar(
                        SolicitudPedido(
                            nombre = nombre,
                            telefono = telefono,
                            correo = correo,
                            direccion = direccion,
                            producto = productoSeleccionado,
                            cantidad = cantidad,
                            notas = notas
                        )
                    )
                },
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.boton_registrar))
            }

            if (mensaje != null) {
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
        }
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
