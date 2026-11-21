package cl.duoc.pmy2201.pizzeria.ui

import cl.duoc.pmy2201.pizzeria.R
import cl.duoc.pmy2201.pizzeria.model.Cliente
import cl.duoc.pmy2201.pizzeria.model.ItemPedido
import cl.duoc.pmy2201.pizzeria.model.Pedido
import cl.duoc.pmy2201.pizzeria.model.PizzaClasica
import cl.duoc.pmy2201.pizzeria.service.CampoPedido
import cl.duoc.pmy2201.pizzeria.service.ResultadoSolicitud
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MensajeResultadoTest {

    private val ana: Cliente = Cliente(
        nombre = "Ana Pérez",
        telefono = "+56931717552",
        correo = "ana@example.com",
        direccion = "Av. Siempre Viva 123"
    )

    private val pepperoni: PizzaClasica = PizzaClasica(
        nombre = "Pepperoni",
        precio = 11990.0,
        ingredientes = mapOf("Masa" to 1, "Pepperoni" to 80)
    )

    /** Fecha fuera del periodo de promoción, para que el total sea el precio sin descuentos. */
    private val pedido: Pedido = Pedido(
        idPedido = 7,
        cliente = ana,
        items = listOf(ItemPedido(producto = pepperoni, cantidad = 1)),
        notas = null,
        fecha = LocalDate.of(2026, 12, 1)
    )

    @Test
    fun datosInvalidosNoGeneranMensaje() {
        val mensaje: MensajeOperador? = mensajeResultado(ResultadoSolicitud.DatosInvalidos(campos = setOf(CampoPedido.NOMBRE)))

        assertNull(mensaje)
    }

    @Test
    fun registradoDeClienteNuevoMuestraIdNombreYTotal() {
        val mensaje: MensajeOperador? = mensajeResultado(ResultadoSolicitud.Registrado(pedido = pedido, clienteRecurrente = false))

        assertNotNull(mensaje)
        assertEquals(R.string.mensaje_registrado, mensaje.texto)
        assertEquals(listOf<Any>(7, "Ana Pérez", "\$11.990"), mensaje.argumentos)
        assertFalse(mensaje.clienteFrecuente)
        assertFalse(mensaje.esError)
    }

    @Test
    fun registradoDeClienteRecurrenteIndicaClienteFrecuente() {
        val mensaje: MensajeOperador? = mensajeResultado(ResultadoSolicitud.Registrado(pedido = pedido, clienteRecurrente = true))

        assertNotNull(mensaje)
        assertTrue(mensaje.clienteFrecuente)
    }

    @Test
    fun sinStockConClienteAgregadoListaIngredientesOrdenados() {
        val resultado: ResultadoSolicitud = ResultadoSolicitud.SinStock(
            ingredientesFaltantes = setOf("Queso mozzarella", "Albahaca", "Queso azul"),
            clienteAgregado = true
        )

        val mensaje: MensajeOperador? = mensajeResultado(resultado)

        assertNotNull(mensaje)
        assertEquals(R.string.mensaje_sin_stock_cliente_agregado, mensaje.texto)
        assertEquals(listOf<Any>("Albahaca, Queso azul, Queso mozzarella"), mensaje.argumentos)
        assertTrue(mensaje.esError)
    }

    @Test
    fun sinStockSinClienteAgregadoUsaMensajeDePedidoNoRegistrado() {
        val resultado: ResultadoSolicitud = ResultadoSolicitud.SinStock(
            ingredientesFaltantes = setOf("Queso azul"),
            clienteAgregado = false
        )

        val mensaje: MensajeOperador? = mensajeResultado(resultado)

        assertNotNull(mensaje)
        assertEquals(R.string.mensaje_sin_stock, mensaje.texto)
        assertTrue(mensaje.esError)
    }

    @Test
    fun sinCapacidadUsaElTextoSegunSiSeAgregoElCliente() {
        val conClienteAgregado: MensajeOperador? = mensajeResultado(ResultadoSolicitud.SinCapacidad(clienteAgregado = true))
        val sinClienteAgregado: MensajeOperador? = mensajeResultado(ResultadoSolicitud.SinCapacidad(clienteAgregado = false))

        assertNotNull(conClienteAgregado)
        assertNotNull(sinClienteAgregado)
        assertEquals(R.string.mensaje_sin_capacidad_cliente_agregado, conClienteAgregado.texto)
        assertEquals(R.string.mensaje_sin_capacidad, sinClienteAgregado.texto)
        assertTrue(conClienteAgregado.esError)
        assertTrue(sinClienteAgregado.esError)
    }
}
