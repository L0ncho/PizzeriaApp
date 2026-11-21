package cl.duoc.pmy2201.pizzeria.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import cl.duoc.pmy2201.pizzeria.data.InventarioRepository
import cl.duoc.pmy2201.pizzeria.data.ProductoRepository
import cl.duoc.pmy2201.pizzeria.data.RepartidorRepository
import cl.duoc.pmy2201.pizzeria.model.PizzaClasica
import cl.duoc.pmy2201.pizzeria.model.Producto
import cl.duoc.pmy2201.pizzeria.model.Repartidor
import cl.duoc.pmy2201.pizzeria.model.ResumenDia
import cl.duoc.pmy2201.pizzeria.service.ResultadoSolicitud
import cl.duoc.pmy2201.pizzeria.service.SolicitudPedido
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class PedidosViewModelTest {

    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private val dispatcher: TestDispatcher = StandardTestDispatcher()

    private class ProductoRepositoryEnMemoria(
        private val productos: List<Producto>
    ) : ProductoRepository {
        override fun obtenerTodos(): List<Producto> = productos
        override fun erroresDeCarga(): List<String> = emptyList()
    }

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

    private val napolitana: PizzaClasica = PizzaClasica(
        nombre = "Napolitana",
        precio = 10990.0,
        ingredientes = mapOf("Masa" to 1, "Tomate" to 150)
    )

    private val resumenVacio: ResumenDia = ResumenDia(totalClientes = 0, totalPedidos = 0, nombreUltimoCliente = null)

    private val solicitudValida: SolicitudPedido = SolicitudPedido(
        nombre = "Ana Pérez",
        telefono = "+56931717552",
        correo = "ana@example.com",
        direccion = "Av. Siempre Viva 123",
        cantidades = mapOf(pepperoni to 1),
        notas = ""
    )

    @BeforeTest
    fun configurarDispatcherPrincipal() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun restaurarDispatcherPrincipal() {
        Dispatchers.resetMain()
    }

    private fun crearViewModel(): PedidosViewModel = PedidosViewModel(
        productoRepository = ProductoRepositoryEnMemoria(listOf(pepperoni, napolitana)),
        inventarioRepository = InventarioRepositoryEnMemoria(
            mutableMapOf("Masa" to 20, "Pepperoni" to 800, "Tomate" to 1500)
        ),
        repartidorRepository = RepartidorRepositoryEnMemoria(
            listOf(
                Repartidor(id = 1, nombre = "Matías Rojas", telefono = "+56911111111", disponible = true, distanciaKm = 3.5)
            )
        ),
        dispatcherCarga = dispatcher
    )

    @Test
    fun cargaTerminaConProductosYResumenVacio() = runTest(dispatcher) {
        val viewModel: PedidosViewModel = crearViewModel()

        assertEquals(true, viewModel.cargando.value)

        advanceUntilIdle()

        assertEquals(false, viewModel.cargando.value)
        assertEquals(listOf<Producto>(pepperoni, napolitana), viewModel.productos.value)
        assertEquals(resumenVacio, viewModel.resumen.value)
    }

    @Test
    fun registrarPedidoMientrasCargaNoRegistraNada() = runTest(dispatcher) {
        val viewModel: PedidosViewModel = crearViewModel()

        viewModel.registrarPedido(solicitudValida)

        assertEquals(true, viewModel.cargando.value)
        assertEquals(0, viewModel.resumen.value?.totalPedidos)
        assertNull(viewModel.ultimoResultado.value)
    }

    @Test
    fun pedidoValidoTrasLaCargaQuedaRegistrado() = runTest(dispatcher) {
        val viewModel: PedidosViewModel = crearViewModel()
        advanceUntilIdle()

        viewModel.registrarPedido(solicitudValida)

        assertIs<ResultadoSolicitud.Registrado>(viewModel.ultimoResultado.value)
        assertEquals(
            ResumenDia(totalClientes = 1, totalPedidos = 1, nombreUltimoCliente = "Ana Pérez"),
            viewModel.resumen.value
        )
        assertEquals(1, viewModel.pedidos.value?.size)
    }

    @Test
    fun datosInvalidosTrasLaCargaNoCambianElResumen() = runTest(dispatcher) {
        val viewModel: PedidosViewModel = crearViewModel()
        advanceUntilIdle()

        viewModel.registrarPedido(solicitudValida.copy(nombre = "A", cantidades = emptyMap()))

        assertIs<ResultadoSolicitud.DatosInvalidos>(viewModel.ultimoResultado.value)
        assertEquals(resumenVacio, viewModel.resumen.value)
    }

    @Test
    fun trasLaCargaHayDisponibilidadParaCadaProducto() = runTest(dispatcher) {
        val viewModel: PedidosViewModel = crearViewModel()
        advanceUntilIdle()

        assertEquals(mapOf<Producto, Boolean>(pepperoni to true, napolitana to true), viewModel.disponibilidad.value)
    }

    @Test
    fun pedidoQueAgotaUnIngredienteDejaSuProductoNoDisponible() = runTest(dispatcher) {
        val viewModel: PedidosViewModel = crearViewModel()
        advanceUntilIdle()

        viewModel.registrarPedido(solicitudValida.copy(cantidades = mapOf(pepperoni to 10)))

        assertIs<ResultadoSolicitud.Registrado>(viewModel.ultimoResultado.value)
        assertEquals(mapOf<Producto, Boolean>(pepperoni to false, napolitana to true), viewModel.disponibilidad.value)
    }
}
