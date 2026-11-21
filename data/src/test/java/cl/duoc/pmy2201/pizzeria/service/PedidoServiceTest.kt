package cl.duoc.pmy2201.pizzeria.service

import cl.duoc.pmy2201.pizzeria.data.InventarioRepository
import cl.duoc.pmy2201.pizzeria.data.RepartidorRepository
import cl.duoc.pmy2201.pizzeria.model.Cliente
import cl.duoc.pmy2201.pizzeria.model.ItemPedido
import cl.duoc.pmy2201.pizzeria.model.Pedido
import cl.duoc.pmy2201.pizzeria.model.PizzaClasica
import cl.duoc.pmy2201.pizzeria.model.Repartidor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class PedidoServiceTest {

    private class InventarioRepositoryEnMemoria(
        private val inventario: MutableMap<String, Int>
    ) : InventarioRepository {
        override fun obtenerInventario(): MutableMap<String, Int> = inventario
        override fun unidadDe(ingrediente: String): String? = if (inventario.containsKey(ingrediente)) "g" else null
        override fun erroresDeCarga(): List<String> = emptyList()
    }

    private class RepartidorRepositoryEnMemoria(
        private val repartidores: List<Repartidor>
    ) : RepartidorRepository {
        override fun obtenerTodos(): List<Repartidor> = repartidores
        override fun erroresDeCarga(): List<String> = emptyList()
    }

    private val pepperoni: PizzaClasica = PizzaClasica(
        nombre = "Pepperoni",
        precio = 11990.0,
        ingredientes = mapOf("Masa" to 1, "Pepperoni" to 80)
    )

    private val cliente: Cliente = Cliente(
        nombre = "Ana Pérez",
        telefono = "+56931717552",
        correo = "ana@example.com",
        direccion = "Av. Siempre Viva 123"
    )

    private val itemsDeUnaPizza: List<ItemPedido> = listOf(ItemPedido(producto = pepperoni, cantidad = 1))

    /** Stock suficiente para más de 101 pedidos de una pizza Pepperoni. */
    private val inventario: MutableMap<String, Int> = mutableMapOf(
        "Masa" to 200,
        "Pepperoni" to 20000
    )

    /**
     * Crea el servicio con un solo repartidor: [AsignacionRepartidorService.asignar] retorna null cuando
     * ya no hay repartidores disponibles y el pedido igual se registra, por lo que no se necesitan más.
     */
    private fun crearServicio(): PedidoService = PedidoService(
        inventarioService = InventarioService(InventarioRepositoryEnMemoria(inventario)),
        asignacionRepartidorService = AsignacionRepartidorService(
            RepartidorRepositoryEnMemoria(
                listOf(
                    Repartidor(id = 1, nombre = "Matías Rojas", telefono = "+56911111111", disponible = true, distanciaKm = 3.5)
                )
            )
        )
    )

    private fun registrarPedidos(servicio: PedidoService, cantidad: Int) {
        repeat(cantidad) {
            val resultado: ResultadoRegistro = servicio.registrarPedido(cliente = cliente, items = itemsDeUnaPizza, notas = null)
            assertIs<ResultadoRegistro.Registrado>(resultado)
        }
    }

    @Test
    fun conNoventaYNuevePedidosElPedidoCienQuedaRegistrado() {
        val servicio: PedidoService = crearServicio()
        registrarPedidos(servicio = servicio, cantidad = 99)

        val resultado: ResultadoRegistro = servicio.registrarPedido(cliente = cliente, items = itemsDeUnaPizza, notas = null)

        val registrado: ResultadoRegistro.Registrado = assertIs<ResultadoRegistro.Registrado>(resultado)
        assertEquals(100, registrado.pedido.idPedido)
        assertEquals(100, servicio.pedidosDelDia().size)
    }

    @Test
    fun conCienPedidosElPedidoCientoUnoRetornaSinCapacidad() {
        val servicio: PedidoService = crearServicio()
        registrarPedidos(servicio = servicio, cantidad = 100)

        val resultado: ResultadoRegistro = servicio.registrarPedido(cliente = cliente, items = itemsDeUnaPizza, notas = null)

        assertSame(ResultadoRegistro.SinCapacidad, resultado)
    }

    @Test
    fun pedidoCientoUnoSinCapacidadNoSeAgregaALosPedidosDelDia() {
        val servicio: PedidoService = crearServicio()
        registrarPedidos(servicio = servicio, cantidad = 100)

        servicio.registrarPedido(cliente = cliente, items = itemsDeUnaPizza, notas = null)

        val pedidos: Array<Pedido> = servicio.pedidosDelDia()
        assertEquals(100, pedidos.size)
        assertEquals((1..100).toList(), pedidos.map { pedido: Pedido -> pedido.idPedido })
    }

    @Test
    fun pedidoCientoUnoSinCapacidadNoDescuentaIngredientes() {
        val servicio: PedidoService = crearServicio()
        registrarPedidos(servicio = servicio, cantidad = 100)
        val inventarioAntes: Map<String, Int> = inventario.toMap()

        servicio.registrarPedido(cliente = cliente, items = itemsDeUnaPizza, notas = null)

        assertEquals(inventarioAntes, inventario.toMap())
    }
}
