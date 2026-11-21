package cl.duoc.pmy2201.pizzeria.service

import cl.duoc.pmy2201.pizzeria.data.InventarioRepository
import cl.duoc.pmy2201.pizzeria.data.RepartidorRepository
import cl.duoc.pmy2201.pizzeria.model.Categoria
import cl.duoc.pmy2201.pizzeria.model.ItemPedido
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

    private val napolitana: PizzaClasica = PizzaClasica(
        nombre = "Napolitana",
        precio = 10990.0,
        ingredientes = mapOf("Masa" to 1, "Tomate" to 150)
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
            "Tomate" to 1500,
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
        cantidades: Map<Producto, Int> = mapOf(pepperoni to 1),
        direccion: String = "Av. Siempre Viva 123"
    ): SolicitudPedido = SolicitudPedido(
        nombre = "Ana Pérez",
        telefono = telefono,
        correo = correo,
        direccion = direccion,
        cantidades = cantidades,
        notas = ""
    )

    private fun solicitudDePedro(): SolicitudPedido = SolicitudPedido(
        nombre = "Pedro Soto",
        telefono = "+56987654321",
        correo = "pedro@example.com",
        direccion = "Calle Falsa 456",
        cantidades = mapOf(pepperoni to 1),
        notas = ""
    )

    @Test
    fun datosInvalidosRetornanTodosLosCamposSinRegistrarNada() {
        val registro: RegistroPedidoService = crearRegistro()
        val camposCliente: Set<CampoPedido> =
            setOf(CampoPedido.NOMBRE, CampoPedido.TELEFONO, CampoPedido.CORREO, CampoPedido.DIRECCION)
        val casos: Map<Map<Producto, Int>, CampoPedido> = mapOf(
            emptyMap<Producto, Int>() to CampoPedido.PRODUCTO,
            mapOf<Producto, Int>(pepperoni to 101) to CampoPedido.CANTIDAD
        )

        casos.forEach { (cantidades: Map<Producto, Int>, campoPizzas: CampoPedido) ->
            val solicitud: SolicitudPedido = SolicitudPedido(
                nombre = "A",
                telefono = "123",
                correo = "ana@",
                direccion = "Av 1",
                cantidades = cantidades,
                notas = ""
            )

            val resultado: ResultadoSolicitud = registro.registrar(solicitud)

            val invalidos: ResultadoSolicitud.DatosInvalidos = assertIs<ResultadoSolicitud.DatosInvalidos>(resultado)
            assertEquals(camposCliente + campoPizzas, invalidos.campos)
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

        val resultado: ResultadoSolicitud = registro.registrar(solicitudDeAna(cantidades = mapOf(cuatroQuesos to 3)))

        val sinStock: ResultadoSolicitud.SinStock = assertIs<ResultadoSolicitud.SinStock>(resultado)
        assertTrue("Queso azul" in sinStock.ingredientesFaltantes)
        assertTrue(sinStock.clienteAgregado)
        assertEquals(ResumenDia(totalClientes = 1, totalPedidos = 0, nombreUltimoCliente = "Ana Pérez"), registro.resumen())
    }

    @Test
    fun primerPedidoRegistradoTrasSinStockNoEsRecurrente() {
        val registro: RegistroPedidoService = crearRegistro()
        registro.registrar(solicitudDeAna(cantidades = mapOf(cuatroQuesos to 3)))

        val resultado: ResultadoSolicitud = registro.registrar(solicitudDeAna(cantidades = mapOf(pepperoni to 1)))

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

    @Test
    fun pedidoConVariasPizzasQuedaRegistradoConTodosLosItems() {
        val registro: RegistroPedidoService = crearRegistro()

        val resultado: ResultadoSolicitud = registro.registrar(
            solicitudDeAna(cantidades = mapOf(pepperoni to 2, napolitana to 1))
        )

        val registrado: ResultadoSolicitud.Registrado = assertIs<ResultadoSolicitud.Registrado>(resultado)
        assertEquals(
            listOf(ItemPedido(producto = pepperoni, cantidad = 2), ItemPedido(producto = napolitana, cantidad = 1)),
            registrado.pedido.items
        )
    }

    @Test
    fun pizzasConCantidadCeroSeIgnoran() {
        val registro: RegistroPedidoService = crearRegistro()

        val resultado: ResultadoSolicitud = registro.registrar(
            solicitudDeAna(cantidades = mapOf(pepperoni to 0, napolitana to 1))
        )

        val registrado: ResultadoSolicitud.Registrado = assertIs<ResultadoSolicitud.Registrado>(resultado)
        assertEquals(listOf(ItemPedido(producto = napolitana, cantidad = 1)), registrado.pedido.items)
    }

    @Test
    fun soloCantidadesEnCeroMarcanProductoInvalido() {
        val registro: RegistroPedidoService = crearRegistro()

        val resultado: ResultadoSolicitud = registro.registrar(
            solicitudDeAna(cantidades = mapOf(pepperoni to 0, napolitana to 0))
        )

        val invalidos: ResultadoSolicitud.DatosInvalidos = assertIs<ResultadoSolicitud.DatosInvalidos>(resultado)
        assertEquals(setOf(CampoPedido.PRODUCTO), invalidos.campos)
    }

    @Test
    fun pizzasQueSumanMasDeCienMarcanCantidadInvalida() {
        val registro: RegistroPedidoService = crearRegistro()

        val resultado: ResultadoSolicitud = registro.registrar(
            solicitudDeAna(cantidades = mapOf(pepperoni to 60, napolitana to 50))
        )

        val invalidos: ResultadoSolicitud.DatosInvalidos = assertIs<ResultadoSolicitud.DatosInvalidos>(resultado)
        assertEquals(setOf(CampoPedido.CANTIDAD), invalidos.campos)
        assertEquals(0, registro.resumen().totalPedidos)
    }
}
