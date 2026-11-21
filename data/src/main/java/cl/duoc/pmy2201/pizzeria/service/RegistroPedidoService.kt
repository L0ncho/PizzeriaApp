package cl.duoc.pmy2201.pizzeria.service

import cl.duoc.pmy2201.pizzeria.model.Cliente
import cl.duoc.pmy2201.pizzeria.model.ItemPedido
import cl.duoc.pmy2201.pizzeria.model.Pedido
import cl.duoc.pmy2201.pizzeria.model.Producto
import cl.duoc.pmy2201.pizzeria.model.ResumenDia
import cl.duoc.pmy2201.pizzeria.util.esCantidadValida
import cl.duoc.pmy2201.pizzeria.util.esCorreoValido
import cl.duoc.pmy2201.pizzeria.util.esDireccionValida
import cl.duoc.pmy2201.pizzeria.util.esNombreValido
import cl.duoc.pmy2201.pizzeria.util.normalizarTelefono

/**
 * Registra pedidos a partir de los datos del formulario y entrega el resumen del día.
 * Coordina [ClienteService], para identificar o agregar al cliente, y [PedidoService], para registrar el pedido.
 */
class RegistroPedidoService(
    private val clienteService: ClienteService,
    private val pedidoService: PedidoService
) {

    /**
     * Registra el pedido descrito en [solicitud]:
     * - [ResultadoSolicitud.DatosInvalidos] con todos los campos inválidos, si hay alguno; no se registra nada.
     *   El correo vacío o en blanco se considera "sin correo" y es válido.
     * - Si el cliente no existe (ver [ClienteService.buscar]), se registra antes de intentar el pedido,
     *   por lo que queda agregado aunque el pedido luego no se registre.
     * - [ResultadoSolicitud.Registrado] si el pedido se registró; el cliente es recurrente si ya tenía
     *   al menos un pedido en el día antes de este.
     * - [ResultadoSolicitud.SinStock] si faltan ingredientes, y [ResultadoSolicitud.SinCapacidad]
     *   si se alcanzó la capacidad del día; ambos indican si el cliente fue agregado.
     */
    fun registrar(solicitud: SolicitudPedido): ResultadoSolicitud {
        val telefono: String? = normalizarTelefono(solicitud.telefono)
        val correo: String? = solicitud.correo.trim().takeIf { texto: String -> texto.isNotEmpty() }
        val producto: Producto? = solicitud.producto
        val cantidad: Int? = solicitud.cantidad.trim().toIntOrNull()
            ?.takeIf { valor: Int -> esCantidadValida(valor) }

        val camposInvalidos: Set<CampoPedido> = buildSet {
            if (!esNombreValido(solicitud.nombre)) add(CampoPedido.NOMBRE)
            if (telefono == null) add(CampoPedido.TELEFONO)
            if (correo != null && !esCorreoValido(correo)) add(CampoPedido.CORREO)
            if (!esDireccionValida(solicitud.direccion)) add(CampoPedido.DIRECCION)
            if (producto == null) add(CampoPedido.PRODUCTO)
            if (cantidad == null) add(CampoPedido.CANTIDAD)
        }
        if (camposInvalidos.isNotEmpty() || telefono == null || producto == null || cantidad == null) {
            return ResultadoSolicitud.DatosInvalidos(campos = camposInvalidos)
        }

        val nombre: String = solicitud.nombre.trim()
        val existente: Cliente? = clienteService.buscar(nombre = nombre, correo = correo, telefono = telefono)
        val clienteAgregado: Boolean = existente == null
        val cliente: Cliente = existente ?: registrarCliente(
            nombre = nombre,
            telefono = telefono,
            correo = correo,
            direccion = solicitud.direccion.trim()
        )
        val clienteRecurrente: Boolean = pedidoService.pedidosDelDia().any { pedido: Pedido -> pedido.cliente == cliente }
        val notas: String? = solicitud.notas.trim().takeIf { texto: String -> texto.isNotEmpty() }

        val resultado: ResultadoRegistro = pedidoService.registrarPedido(
            cliente = cliente,
            items = listOf(ItemPedido(producto = producto, cantidad = cantidad)),
            notas = notas
        )
        return when (resultado) {
            is ResultadoRegistro.Registrado ->
                ResultadoSolicitud.Registrado(pedido = resultado.pedido, clienteRecurrente = clienteRecurrente)
            is ResultadoRegistro.Rechazado -> ResultadoSolicitud.SinStock(
                ingredientesFaltantes = resultado.resultado.ingredientesFaltantes,
                clienteAgregado = clienteAgregado
            )
            ResultadoRegistro.SinCapacidad -> ResultadoSolicitud.SinCapacidad(clienteAgregado = clienteAgregado)
        }
    }

    /**
     * Retorna el resumen del día: cantidad de clientes, cantidad de pedidos y nombre del último cliente registrado.
     */
    fun resumen(): ResumenDia = ResumenDia(
        totalClientes = clienteService.clientesDelDia().size,
        totalPedidos = pedidoService.pedidosDelDia().size,
        nombreUltimoCliente = clienteService.ultimoRegistrado()?.nombre
    )

    /** Crea un cliente nuevo, lo registra en [clienteService] y retorna esa misma instancia. */
    private fun registrarCliente(nombre: String, telefono: String, correo: String?, direccion: String): Cliente {
        val nuevo: Cliente = Cliente(nombre = nombre, telefono = telefono, correo = correo, direccion = direccion)
        clienteService.registrar(nuevo)
        return nuevo
    }
}
