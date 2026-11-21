package cl.duoc.pmy2201.pizzeria.model

import cl.duoc.pmy2201.pizzeria.service.PedidoService
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class PedidoTest {

    private val tolerancia: Double = 0.01

    private val fecha: LocalDate = LocalDate.of(2026, 11, 4)

    private val pepperoni: PizzaClasica = PizzaClasica(
        nombre = "Pepperoni",
        precio = 11990.0,
        ingredientes = mapOf("Masa" to 1, "Pepperoni" to 80)
    )

    private val napolitana: PizzaClasica = PizzaClasica(
        nombre = "Napolitana",
        precio = 10990.0,
        ingredientes = mapOf("Masa" to 1, "Tomate" to 150)
    )

    private val ana: Cliente = Cliente(
        nombre = "Ana Pérez",
        telefono = "+56931717552",
        correo = "ana@example.com",
        direccion = "Av. Siempre Viva 123"
    )

    private val pedro: Cliente = Cliente(
        nombre = "Pedro Soto",
        telefono = "+56223456789",
        correo = "pedro@example.com",
        direccion = "Calle Falsa 456"
    )

    private fun crearPedido(idPedido: Int, cliente: Cliente, producto: Producto, cantidad: Int): Pedido = Pedido(
        idPedido = idPedido,
        cliente = cliente,
        items = listOf(ItemPedido(producto = producto, cantidad = cantidad)),
        notas = null,
        fecha = fecha
    )

    @Test
    fun sumarPepperoniYNapolitanaDelMismoClienteCombinaLineasYRecalculaTotal() {
        val pedidoPepperoni: Pedido = crearPedido(idPedido = 1, cliente = ana, producto = pepperoni, cantidad = 2)
        val pedidoNapolitana: Pedido = crearPedido(idPedido = 2, cliente = ana, producto = napolitana, cantidad = 2)

        val combinado: Pedido = pedidoPepperoni + pedidoNapolitana

        assertEquals(2, combinado.items.size)
        assertEquals(4, combinado.cantidadPizzas())
        assertEquals(34470.0, combinado.calcularTotal().total, tolerancia)
    }

    @Test
    fun sumarPepperoniConPepperoniDejaUnaSolaLineaConCantidadSumada() {
        val primero: Pedido = crearPedido(idPedido = 1, cliente = ana, producto = pepperoni, cantidad = 2)
        val segundo: Pedido = crearPedido(idPedido = 2, cliente = ana, producto = pepperoni, cantidad = 2)

        val combinado: Pedido = primero + segundo

        assertEquals(1, combinado.items.size)
        assertEquals(pepperoni, combinado.items.first().producto)
        assertEquals(4, combinado.items.first().cantidad)
    }

    @Test
    fun sumarPedidosDeClientesDistintosLanzaExcepcion() {
        val pedidoAna: Pedido = crearPedido(idPedido = 1, cliente = ana, producto = pepperoni, cantidad = 2)
        val pedidoPedro: Pedido = crearPedido(idPedido = 2, cliente = pedro, producto = pepperoni, cantidad = 2)

        assertFailsWith<IllegalArgumentException> {
            pedidoAna + pedidoPedro
        }
    }

    @Test
    fun sumarPedidosDeFechasDistintasLanzaExcepcion() {
        val pedidoHoy: Pedido = crearPedido(idPedido = 1, cliente = ana, producto = pepperoni, cantidad = 2)
        val pedidoManana: Pedido = Pedido(
            idPedido = 2,
            cliente = ana,
            items = listOf(ItemPedido(producto = napolitana, cantidad = 1)),
            notas = null,
            fecha = fecha.plusDays(1)
        )

        assertFailsWith<IllegalArgumentException> {
            pedidoHoy + pedidoManana
        }
    }

    @Test
    fun pedidoCombinadoTieneIdDistintoDeLosOriginales() {
        val primero: Pedido = crearPedido(idPedido = 1, cliente = ana, producto = pepperoni, cantidad = 2)
        val segundo: Pedido = crearPedido(idPedido = 2, cliente = ana, producto = napolitana, cantidad = 1)

        val combinado: Pedido = primero + segundo

        assertNotEquals(primero.idPedido, combinado.idPedido)
        assertNotEquals(segundo.idPedido, combinado.idPedido)
        assertTrue(combinado.idPedido >= Pedido.ID_INICIAL_COMBINADOS)
    }

    @Test
    fun dosCombinacionesSeguidasTienenIdsDistintos() {
        val primero: Pedido = crearPedido(idPedido = 1, cliente = ana, producto = pepperoni, cantidad = 2)
        val segundo: Pedido = crearPedido(idPedido = 2, cliente = ana, producto = napolitana, cantidad = 1)

        val combinado1: Pedido = primero + segundo
        val combinado2: Pedido = primero + segundo

        assertNotEquals(combinado1.idPedido, combinado2.idPedido)
    }

    @Test
    fun idInicialCombinadosEsMayorQueLaCapacidadDiaria() {
        assertTrue(PedidoService.CAPACIDAD_PEDIDOS_DIA < Pedido.ID_INICIAL_COMBINADOS)
    }

    @Test
    fun desestructuracionEntregaClienteProductosYTotal() {
        val pedido: Pedido = crearPedido(idPedido = 1, cliente = ana, producto = pepperoni, cantidad = 2)

        val (cliente: Cliente, productos: List<ItemPedido>, total: Double) = pedido

        assertSame(ana, cliente)
        assertEquals(pedido.items, productos)
        assertEquals(pedido.calcularTotal().total, total, tolerancia)
    }

    @Test
    fun pedidoSinDireccionEntregaUsaLaDireccionDelCliente() {
        val pedido: Pedido = crearPedido(idPedido = 1, cliente = ana, producto = pepperoni, cantidad = 1)

        assertEquals("Av. Siempre Viva 123", pedido.direccionEntrega)
    }

    @Test
    fun sumarPedidosConDireccionesDeEntregaDistintasLanzaExcepcion() {
        val pedidoCasa: Pedido = crearPedido(idPedido = 1, cliente = ana, producto = pepperoni, cantidad = 1)
        val pedidoOficina: Pedido = Pedido(
            idPedido = 2,
            cliente = ana,
            items = listOf(ItemPedido(producto = napolitana, cantidad = 1)),
            notas = null,
            fecha = fecha,
            direccionEntrega = "Calle Oficina 789"
        )

        assertFailsWith<IllegalArgumentException> {
            pedidoCasa + pedidoOficina
        }
    }

    @Test
    fun sumarPedidosConMismaDireccionConOtrasMayusculasYEspaciosConservaLaDelPrimero() {
        val primero: Pedido = Pedido(
            idPedido = 1,
            cliente = ana,
            items = listOf(ItemPedido(producto = pepperoni, cantidad = 1)),
            notas = null,
            fecha = fecha,
            direccionEntrega = "Calle Oficina 789"
        )
        val segundo: Pedido = Pedido(
            idPedido = 2,
            cliente = ana,
            items = listOf(ItemPedido(producto = napolitana, cantidad = 1)),
            notas = null,
            fecha = fecha,
            direccionEntrega = "  calle OFICINA 789 "
        )

        val combinado: Pedido = primero + segundo

        assertEquals("Calle Oficina 789", combinado.direccionEntrega)
    }

    @Test
    fun cambiarEstadoRecorreEnPreparacionListoEnCaminoYEntregado() {
        val pedido: Pedido = crearPedido(idPedido = 1, cliente = ana, producto = pepperoni, cantidad = 1)
        val recorridoEsperado: List<EstadoPedido> = listOf(
            EstadoPedido.LISTO,
            EstadoPedido.EN_CAMINO,
            EstadoPedido.ENTREGADO
        )

        assertEquals(EstadoPedido.EN_PREPARACION, pedido.estado)
        recorridoEsperado.forEach { estadoEsperado: EstadoPedido ->
            assertEquals(estadoEsperado, pedido.siguienteEstado())
            assertTrue(pedido.cambiarEstado())
            assertEquals(estadoEsperado, pedido.estado)
        }
    }

    @Test
    fun siguienteEstadoDePedidoEntregadoEsNull() {
        val pedido: Pedido = crearPedidoEntregado()

        assertNull(pedido.siguienteEstado())
    }

    @Test
    fun cambiarEstadoDePedidoEntregadoRetornaFalseYNoCambiaElEstado() {
        val pedido: Pedido = crearPedidoEntregado()

        val cambio: Boolean = pedido.cambiarEstado()

        assertFalse(cambio)
        assertEquals(EstadoPedido.ENTREGADO, pedido.estado)
    }

    private fun crearPedidoEntregado(): Pedido {
        val pedido: Pedido = crearPedido(idPedido = 1, cliente = ana, producto = pepperoni, cantidad = 1)
        repeat(3) { pedido.cambiarEstado() }
        assertEquals(EstadoPedido.ENTREGADO, pedido.estado)
        return pedido
    }
}
