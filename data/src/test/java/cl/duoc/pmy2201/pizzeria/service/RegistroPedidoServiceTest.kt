package cl.duoc.pmy2201.pizzeria.service

import cl.duoc.pmy2201.pizzeria.data.InventarioRepository
import cl.duoc.pmy2201.pizzeria.data.RepartidorRepository
import cl.duoc.pmy2201.pizzeria.model.Categoria
import cl.duoc.pmy2201.pizzeria.model.PizzaClasica
import cl.duoc.pmy2201.pizzeria.model.Producto
import cl.duoc.pmy2201.pizzeria.model.Repartidor
import cl.duoc.pmy2201.pizzeria.model.ResumenDia
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RegistroPedidoServiceTest {

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

    private val cuatroQuesos: Producto = Producto(
        nombre = "Cuatro Quesos",
        precio = 13990.0,
        ingredientes = mapOf("Queso mozzarella" to 150, "Queso azul" to 60),
        categoria = Categoria.PIZZA
    )

    private fun crearRegistro(): RegistroPedidoService {
        val inventario: MutableMap<String, Int> = mutableMapOf(
            "Masa" to 40,
            "Pepperoni" to 2000,
            "Queso mozzarella" to 8000,
            "Queso azul" to 150
        )
        val repartidores: List<Repartidor> = listOf(
            Repartidor(id = 1, nombre = "Matías Rojas", telefono = "+56911111111", disponible = true, distanciaKm = 3.5)
        )
        val pedidoService: PedidoService = PedidoService(
            inventarioService = InventarioService(InventarioRepositoryEnMemoria(inventario)),
            asignacionRepartidorService = AsignacionRepartidorService(RepartidorRepositoryEnMemoria(repartidores))
        )
        return RegistroPedidoService(clienteService = ClienteService(), pedidoService = pedidoService)
    }

    private fun solicitudDeAna(
        correo: String = "ana@example.com",
        telefono: String = "+56931717552",
        producto: Producto? = pepperoni,
        cantidad: String = "1",
        direccion: String = "Av. Siempre Viva 123"
    ): SolicitudPedido = SolicitudPedido(
        nombre = "Ana Pérez",
        telefono = telefono,
        correo = correo,
        direccion = direccion,
        producto = producto,
        cantidad = cantidad,
        notas = ""
    )

    private fun solicitudDePedro(): SolicitudPedido = SolicitudPedido(
        nombre = "Pedro Soto",
        telefono = "+56987654321",
        correo = "pedro@example.com",
        direccion = "Calle Falsa 456",
        producto = pepperoni,
        cantidad = "1",
        notas = ""
    )

    @Test
    fun datosInvalidosRetornanTodosLosCamposSinRegistrarNada() {
        val registro: RegistroPedidoService = crearRegistro()
        val todosLosCampos: Set<CampoPedido> = CampoPedido.entries.toSet()

        listOf("0", "abc", "101").forEach { cantidad: String ->
            val solicitud: SolicitudPedido = SolicitudPedido(
                nombre = "A",
                telefono = "123",
                correo = "ana@",
                direccion = "Av 1",
                producto = null,
                cantidad = cantidad,
                notas = ""
            )

            val resultado: ResultadoSolicitud = registro.registrar(solicitud)

            val invalidos: ResultadoSolicitud.DatosInvalidos = assertIs<ResultadoSolicitud.DatosInvalidos>(resultado)
            assertEquals(todosLosCampos, invalidos.campos)
        }
        val resumen: ResumenDia = registro.resumen()
        assertEquals(0, resumen.totalClientes)
        assertEquals(0, resumen.totalPedidos)
    }

    @Test
    fun correoVacioRegistraPedidoConClienteSinCorreo() {
        val registro: RegistroPedidoService = crearRegistro()

        val resultado: ResultadoSolicitud = registro.registrar(solicitudDeAna(correo = ""))

        val registrado: ResultadoSolicitud.Registrado = assertIs<ResultadoSolicitud.Registrado>(resultado)
        assertNull(registrado.pedido.cliente.correo)
    }

    @Test
    fun primerPedidoDeClienteNuevoNoEsRecurrente() {
        val registro: RegistroPedidoService = crearRegistro()

        val resultado: ResultadoSolicitud = registro.registrar(solicitudDeAna())

        val registrado: ResultadoSolicitud.Registrado = assertIs<ResultadoSolicitud.Registrado>(resultado)
        assertFalse(registrado.clienteRecurrente)
        assertEquals(ResumenDia(totalClientes = 1, totalPedidos = 1, nombreUltimoCliente = "Ana Pérez"), registro.resumen())
    }

    @Test
    fun clienteSinCorreoQueVuelveConTelefonoConEspaciosEsRecurrente() {
        val registro: RegistroPedidoService = crearRegistro()
        registro.registrar(solicitudDeAna(correo = "", telefono = "+56931717552"))

        val resultado: ResultadoSolicitud = registro.registrar(solicitudDeAna(correo = "", telefono = "+56 9 3171 7552"))

        val registrado: ResultadoSolicitud.Registrado = assertIs<ResultadoSolicitud.Registrado>(resultado)
        assertTrue(registrado.clienteRecurrente)
        val resumen: ResumenDia = registro.resumen()
        assertEquals(1, resumen.totalClientes)
        assertEquals(2, resumen.totalPedidos)
    }

    @Test
    fun clienteQueVuelveAPedirNoCambiaElUltimoRegistrado() {
        val registro: RegistroPedidoService = crearRegistro()
        registro.registrar(solicitudDeAna())
        registro.registrar(solicitudDePedro())

        registro.registrar(solicitudDeAna())

        assertEquals("Pedro Soto", registro.resumen().nombreUltimoCliente)
    }

    @Test
    fun pedidoSinStockAgregaAlClienteNuevoSinRegistrarPedido() {
        val registro: RegistroPedidoService = crearRegistro()

        val resultado: ResultadoSolicitud = registro.registrar(solicitudDeAna(producto = cuatroQuesos, cantidad = "3"))

        val sinStock: ResultadoSolicitud.SinStock = assertIs<ResultadoSolicitud.SinStock>(resultado)
        assertTrue("Queso azul" in sinStock.ingredientesFaltantes)
        assertTrue(sinStock.clienteAgregado)
        assertEquals(ResumenDia(totalClientes = 1, totalPedidos = 0, nombreUltimoCliente = "Ana Pérez"), registro.resumen())
    }

    @Test
    fun primerPedidoRegistradoTrasSinStockNoEsRecurrente() {
        val registro: RegistroPedidoService = crearRegistro()
        registro.registrar(solicitudDeAna(producto = cuatroQuesos, cantidad = "3"))

        val resultado: ResultadoSolicitud = registro.registrar(solicitudDeAna(producto = pepperoni, cantidad = "1"))

        val registrado: ResultadoSolicitud.Registrado = assertIs<ResultadoSolicitud.Registrado>(resultado)
        assertFalse(registrado.clienteRecurrente)
        val resumen: ResumenDia = registro.resumen()
        assertEquals(1, resumen.totalClientes)
        assertEquals(1, resumen.totalPedidos)
    }

    @Test
    fun validarClienteConDatosInvalidosRetornaLosCuatroCampos() {
        val registro: RegistroPedidoService = crearRegistro()

        val campos: Set<CampoPedido> = registro.validarCliente(
            nombre = "A",
            telefono = "123",
            correo = "malo",
            direccion = "Av 1"
        )

        assertEquals(
            setOf(CampoPedido.NOMBRE, CampoPedido.TELEFONO, CampoPedido.CORREO, CampoPedido.DIRECCION),
            campos
        )
    }

    @Test
    fun validarClienteConDatosValidosRetornaVacio() {
        val registro: RegistroPedidoService = crearRegistro()

        val campos: Set<CampoPedido> = registro.validarCliente(
            nombre = "Ana Pérez",
            telefono = "+56 9 3171 7552",
            correo = "ana@example.com",
            direccion = "Av. Siempre Viva 123"
        )

        assertTrue(campos.isEmpty())
    }

    @Test
    fun clienteRegistradoQuePideAOtraDireccionConservaSuDireccionOriginal() {
        val registro: RegistroPedidoService = crearRegistro()
        registro.registrar(solicitudDeAna(direccion = "Av. Siempre Viva 123"))

        val resultado: ResultadoSolicitud = registro.registrar(solicitudDeAna(direccion = "  Calle Oficina 789 "))

        val registrado: ResultadoSolicitud.Registrado = assertIs<ResultadoSolicitud.Registrado>(resultado)
        assertEquals("Calle Oficina 789", registrado.pedido.direccionEntrega)
        assertEquals("Av. Siempre Viva 123", registrado.pedido.cliente.direccion)
    }
}
