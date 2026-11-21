package cl.duoc.pmy2201.pizzeria.service

import cl.duoc.pmy2201.pizzeria.model.Producto

/**
 * Datos de un pedido tal como se ingresan en el formulario, todavía sin validar.
 *
 * @property nombre Nombre del cliente.
 * @property telefono Teléfono del cliente.
 * @property correo Correo del cliente; vacío o en blanco si no tiene.
 * @property direccion Dirección de entrega.
 * @property producto Producto elegido, o null si no se eligió ninguno.
 * @property cantidad Cantidad del producto, como texto.
 * @property notas Notas del pedido; vacías o en blanco si no hay.
 */
data class SolicitudPedido(
    val nombre: String,
    val telefono: String,
    val correo: String,
    val direccion: String,
    val producto: Producto?,
    val cantidad: String,
    val notas: String
)
