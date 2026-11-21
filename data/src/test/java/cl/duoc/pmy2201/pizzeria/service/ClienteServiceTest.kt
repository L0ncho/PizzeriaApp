package cl.duoc.pmy2201.pizzeria.service

import cl.duoc.pmy2201.pizzeria.model.Cliente
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ClienteServiceTest {

    private val ana: Cliente = Cliente(
        nombre = "Ana Pérez",
        telefono = "+56931717552",
        correo = "ana@example.com",
        direccion = "Av. Siempre Viva 123"
    )

    @Test
    fun registrarDosVecesElMismoClienteSoloLoAgregaUnaVez() {
        val clienteService: ClienteService = ClienteService()

        val primerRegistro: Boolean = clienteService.registrar(ana)
        val segundoRegistro: Boolean = clienteService.registrar(ana)

        assertTrue(primerRegistro)
        assertFalse(segundoRegistro)
        assertEquals(1, clienteService.clientesDelDia().size)
    }

    @Test
    fun buscarEncuentraAlClienteRegistrado() {
        val clienteService: ClienteService = ClienteService()
        clienteService.registrar(ana)

        val encontrado: Cliente? = clienteService.buscar(
            nombre = "ana pérez",
            correo = "ANA@example.com",
            telefono = "+56931717552"
        )

        assertSame(ana, encontrado)
    }

    @Test
    fun buscarClienteSinCorreoPorNombreYTelefonoLoEncuentra() {
        val clienteService: ClienteService = ClienteService()
        val pedroSinCorreo: Cliente = Cliente(
            nombre = "Pedro Soto",
            telefono = "+56987654321",
            correo = null,
            direccion = "Calle Falsa 456"
        )
        clienteService.registrar(pedroSinCorreo)

        val encontrado: Cliente? = clienteService.buscar(nombre = "pedro soto", correo = null, telefono = "+56 9 8765 4321")

        assertSame(pedroSinCorreo, encontrado)
    }

    @Test
    fun buscarClienteSinCorreoConOtroTelefonoRetornaNull() {
        val clienteService: ClienteService = ClienteService()
        val pedroSinCorreo: Cliente = Cliente(
            nombre = "Pedro Soto",
            telefono = "+56987654321",
            correo = null,
            direccion = "Calle Falsa 456"
        )
        clienteService.registrar(pedroSinCorreo)

        val encontrado: Cliente? = clienteService.buscar(nombre = "Pedro Soto", correo = null, telefono = "+56911111111")

        assertNull(encontrado)
    }

    @Test
    fun ultimoRegistradoEsNullSinClientes() {
        val clienteService: ClienteService = ClienteService()

        val ultimo: Cliente? = clienteService.ultimoRegistrado()

        assertNull(ultimo)
    }

    @Test
    fun ultimoRegistradoEsElSegundoTrasRegistrarDos() {
        val clienteService: ClienteService = ClienteService()
        val pedro: Cliente = Cliente(
            nombre = "Pedro Soto",
            telefono = "+56987654321",
            correo = "pedro@example.com",
            direccion = "Calle Falsa 456"
        )
        clienteService.registrar(ana)
        clienteService.registrar(pedro)

        val ultimo: Cliente? = clienteService.ultimoRegistrado()

        assertSame(pedro, ultimo)
    }

    @Test
    fun registrarOtraVezAlPrimeroNoCambiaElUltimoRegistrado() {
        val clienteService: ClienteService = ClienteService()
        val pedro: Cliente = Cliente(
            nombre = "Pedro Soto",
            telefono = "+56987654321",
            correo = "pedro@example.com",
            direccion = "Calle Falsa 456"
        )
        clienteService.registrar(ana)
        clienteService.registrar(pedro)
        clienteService.registrar(ana)

        val ultimo: Cliente? = clienteService.ultimoRegistrado()

        assertSame(pedro, ultimo)
    }
}
