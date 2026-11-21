package cl.duoc.pmy2201.pizzeria.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cl.duoc.pmy2201.pizzeria.R
import cl.duoc.pmy2201.pizzeria.model.ItemPedido
import cl.duoc.pmy2201.pizzeria.model.Pedido
import cl.duoc.pmy2201.pizzeria.util.FormatoMoneda

/**
 * Lista de los pedidos registrados en el día. Por cada pedido muestra id, cliente, cantidad y producto
 * del primer item y total formateado, y debajo la dirección de entrega del pedido.
 * Se usa el primer item porque el formulario registra un solo producto por pedido.
 * Si no hay pedidos muestra un aviso.
 */
@Composable
fun ListaPedidos(pedidos: List<Pedido>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(R.string.titulo_pedidos), style = MaterialTheme.typography.titleMedium)
        if (pedidos.isEmpty()) {
            Text(text = stringResource(R.string.pedidos_vacio), style = MaterialTheme.typography.bodyLarge)
        } else {
            pedidos.forEach { pedido: Pedido ->
                val item: ItemPedido = pedido.items.first()
                Column {
                    Text(
                        text = stringResource(
                            R.string.pedido_linea,
                            pedido.idPedido,
                            pedido.cliente.nombre,
                            item.cantidad,
                            item.producto.nombre,
                            FormatoMoneda.formatear(pedido.calcularTotal().total)
                        ),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = stringResource(R.string.pedido_entrega, pedido.direccionEntrega),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
