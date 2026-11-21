package cl.duoc.pmy2201.pizzeria.service

import cl.duoc.pmy2201.pizzeria.model.Pedido

/**
 * Resultado de intentar registrar un pedido a partir de una [SolicitudPedido].
 */
sealed class ResultadoSolicitud {

    /**
     * Uno o más campos no son válidos; no se registró ni el cliente ni el pedido.
     *
     * @property campos Todos los campos inválidos.
     */
    data class DatosInvalidos(val campos: Set<CampoPedido>) : ResultadoSolicitud()

    /**
     * El pedido fue registrado.
     *
     * @property pedido Pedido creado.
     * @property clienteRecurrente true si el cliente ya tenía al menos un pedido en el día antes de este.
     */
    data class Registrado(val pedido: Pedido, val clienteRecurrente: Boolean) : ResultadoSolicitud()

    /**
     * Faltan ingredientes; el pedido no se registró y el inventario no se modificó.
     *
     * @property ingredientesFaltantes Nombres de los ingredientes que faltan.
     * @property clienteAgregado true si el cliente era nuevo y quedó registrado igualmente.
     */
    data class SinStock(val ingredientesFaltantes: Set<String>, val clienteAgregado: Boolean) : ResultadoSolicitud()

    /**
     * Ya se alcanzó la capacidad de pedidos del día; el pedido no se registró.
     *
     * @property clienteAgregado true si el cliente era nuevo y quedó registrado igualmente.
     */
    data class SinCapacidad(val clienteAgregado: Boolean) : ResultadoSolicitud()
}
