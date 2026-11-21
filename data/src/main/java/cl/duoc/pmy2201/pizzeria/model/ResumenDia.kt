package cl.duoc.pmy2201.pizzeria.model

/**
 * Resumen de la actividad del día.
 *
 * @property totalClientes Cantidad de clientes registrados en el día.
 * @property totalPedidos Cantidad de pedidos registrados en el día.
 * @property nombreUltimoCliente Nombre del último cliente registrado, o null si no hay clientes.
 */
data class ResumenDia(
    val totalClientes: Int,
    val totalPedidos: Int,
    val nombreUltimoCliente: String?
)
