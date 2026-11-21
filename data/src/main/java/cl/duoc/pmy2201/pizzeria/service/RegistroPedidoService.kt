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
     *   Los datos del cliente se validan con [validarCliente]. Las pizzas con cantidad mayor que 0 en
     *   [SolicitudPedido.cantidades] forman los items del pedido (las que tienen 0 se ignoran):
     *   - [CampoPedido.PRODUCTO] si no queda ningún item.
     *   - [CampoPedido.CANTIDAD] si hay items y alguna cantidad, o la suma de todas, no cumple [esCantidadValida].
     *     No se marca cuando no hay items, para no repetir el error de [CampoPedido.PRODUCTO].
     * - El pedido se registra con todos los items.
     * - Si el cliente no existe (ver [ClienteService.buscar]), se registra antes de intentar el pedido,
     *   por lo que queda agregado aunque el pedido luego no se registre.
     * - La dirección del formulario, sin espacios en los extremos, se usa como dirección de entrega del pedido.
     *   Si el cliente ya estaba registrado, conserva su dirección original: pedir a otra dirección no la modifica.
     * - [ResultadoSolicitud.Registrado] si el pedido se registró; el cliente es recurrente si ya tenía
     *   al menos un pedido en el día antes de este.
     * - [ResultadoSolicitud.SinStock] si faltan ingredientes, y [ResultadoSolicitud.SinCapacidad]
     *   si se alcanzó la capacidad del día; ambos indican si el cliente fue agregado.
     */
    fun registrar(solicitud: SolicitudPedido): ResultadoSolicitud {
        val telefono: String? = normalizarTelefono(solicitud.telefono)
        val correo: String? = solicitud.correo.trim().takeIf { texto: String -> texto.isNotEmpty() }
        val items: List<ItemPedido> = solicitud.cantidades
            .filter { entrada: Map.Entry<Producto, Int> -> entrada.value > 0 }
            .map { entrada: Map.Entry<Producto, Int> -> ItemPedido(producto = entrada.key, cantidad = entrada.value) }
        val cantidadesValidas: Boolean =
            items.all { item: ItemPedido -> esCantidadValida(item.cantidad) } &&
                esCantidadValida(items.sumOf { item: ItemPedido -> item.cantidad })

        val camposInvalidos: Set<CampoPedido> = validarCliente(
            nombre = solicitud.nombre,
            telefono = solicitud.telefono,
            correo = solicitud.correo,
            direccion = solicitud.direccion
        ) + buildSet {
            if (items.isEmpty()) add(CampoPedido.PRODUCTO) else if (!cantidadesValidas) add(CampoPedido.CANTIDAD)
        }
        if (camposInvalidos.isNotEmpty() || telefono == null) {
            return ResultadoSolicitud.DatosInvalidos(campos = camposInvalidos)
        }

        val nombre: String = solicitud.nombre.trim()
        val direccion: String = solicitud.direccion.trim()
        val existente: Cliente? = clienteService.buscar(nombre = nombre, correo = correo, telefono = telefono)
        val clienteAgregado: Boolean = existente == null
        val cliente: Cliente = existente ?: registrarCliente(
            nombre = nombre,
            telefono = telefono,
            correo = correo,
            direccion = direccion
        )
        val clienteRecurrente: Boolean = pedidoService.pedidosDelDia().any { pedido: Pedido -> pedido.cliente == cliente }
        val notas: String? = solicitud.notas.trim().takeIf { texto: String -> texto.isNotEmpty() }

        val resultado: ResultadoRegistro = pedidoService.registrarPedido(
            cliente = cliente,
            items = items,
            notas = notas,
            direccionEntrega = direccion
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
     * Valida solo los datos del cliente y retorna los campos inválidos, o un conjunto vacío si todos son válidos.
     * Solo puede retornar [CampoPedido.NOMBRE], [CampoPedido.TELEFONO], [CampoPedido.CORREO] y [CampoPedido.DIRECCION].
     * Permite revisar al cliente antes de elegir la pizza; [registrar] usa estas mismas reglas.
     * - Nombre: [esNombreValido].
     * - Teléfono: debe poder normalizarse con [normalizarTelefono].
     * - Correo: vacío o en blanco se considera "sin correo" y es válido; si no, [esCorreoValido].
     * - Dirección: [esDireccionValida].
     */
    fun validarCliente(nombre: String, telefono: String, correo: String, direccion: String): Set<CampoPedido> {
        val correoRecortado: String = correo.trim()
        return buildSet {
            if (!esNombreValido(nombre)) add(CampoPedido.NOMBRE)
            if (normalizarTelefono(telefono) == null) add(CampoPedido.TELEFONO)
            if (correoRecortado.isNotEmpty() && !esCorreoValido(correoRecortado)) add(CampoPedido.CORREO)
            if (!esDireccionValida(direccion)) add(CampoPedido.DIRECCION)
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
