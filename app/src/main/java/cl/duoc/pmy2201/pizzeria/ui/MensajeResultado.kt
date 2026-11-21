package cl.duoc.pmy2201.pizzeria.ui

import androidx.annotation.StringRes
import cl.duoc.pmy2201.pizzeria.R
import cl.duoc.pmy2201.pizzeria.model.Pedido
import cl.duoc.pmy2201.pizzeria.service.ResultadoSolicitud
import cl.duoc.pmy2201.pizzeria.util.FormatoMoneda
import java.text.Collator
import java.util.Locale

/**
 * Mensaje para el operador después de intentar registrar un pedido.
 *
 * Guarda el id del recurso de texto y sus argumentos en vez del texto final, para que se resuelva
 * con `stringResource` en la pantalla y [mensajeResultado] se pueda probar sin Android.
 *
 * @property texto Recurso de texto del mensaje.
 * @property argumentos Valores que reemplazan los marcadores del texto, en orden.
 * @property clienteFrecuente true si además se debe indicar que el cliente es frecuente.
 * @property esError true si el mensaje informa que el pedido no se registró.
 */
data class MensajeOperador(
    @StringRes val texto: Int,
    val argumentos: List<Any>,
    val clienteFrecuente: Boolean = false,
    val esError: Boolean
)

/**
 * Convierte [resultado] en el mensaje para el operador:
 * - [ResultadoSolicitud.DatosInvalidos]: null, porque esos errores se muestran bajo cada campo.
 * - [ResultadoSolicitud.Registrado]: id del pedido, nombre del cliente y total formateado con [FormatoMoneda].
 * - [ResultadoSolicitud.SinStock] y [ResultadoSolicitud.SinCapacidad]: mensaje de error que indica si el cliente
 *   quedó agregado; en SinStock, con los ingredientes faltantes en orden alfabético y unidos con ", ".
 */
fun mensajeResultado(resultado: ResultadoSolicitud): MensajeOperador? = when (resultado) {
    is ResultadoSolicitud.DatosInvalidos -> null
    is ResultadoSolicitud.Registrado -> {
        val pedido: Pedido = resultado.pedido
        MensajeOperador(
            texto = R.string.mensaje_registrado,
            argumentos = listOf(pedido.idPedido, pedido.cliente.nombre, FormatoMoneda.formatear(pedido.calcularTotal().total)),
            clienteFrecuente = resultado.clienteRecurrente,
            esError = false
        )
    }
    is ResultadoSolicitud.SinStock -> MensajeOperador(
        texto = if (resultado.clienteAgregado) R.string.mensaje_sin_stock_cliente_agregado else R.string.mensaje_sin_stock,
        argumentos = listOf(resultado.ingredientesFaltantes.sortedWith(ORDEN_ALFABETICO).joinToString(", ")),
        esError = true
    )
    is ResultadoSolicitud.SinCapacidad -> MensajeOperador(
        texto = if (resultado.clienteAgregado) R.string.mensaje_sin_capacidad_cliente_agregado else R.string.mensaje_sin_capacidad,
        argumentos = emptyList(),
        esError = true
    )
}

/** Orden alfabético en español, para que las letras con tilde y la ñ queden en su lugar. */
private val ORDEN_ALFABETICO: Collator = Collator.getInstance(Locale.forLanguageTag("es-CL"))
