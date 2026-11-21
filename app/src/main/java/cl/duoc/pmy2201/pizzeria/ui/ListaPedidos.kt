package cl.duoc.pmy2201.pizzeria.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
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
 * Devuelve las pizzas de [items] como texto, en el orden de los items: cantidad, " x " y nombre,
 * separados por ", ". Ejemplo: "2 x Pepperoni, 1 x Napolitana".
 */
fun textoPizzas(items: List<ItemPedido>): String =
    items.joinToString(", ") { item: ItemPedido -> "${item.cantidad} x ${item.producto.nombre}" }

/**
 * Lista de los pedidos registrados en el día, cada uno en una tarjeta pequeña con id, cliente y total
 * formateado; debajo, todas sus pizzas (ver [textoPizzas]) y la dirección de entrega del pedido.
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
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Text(
                            text = stringResource(
                                R.string.pedido_linea,
                                pedido.idPedido,
                                pedido.cliente.nombre,
                                FormatoMoneda.formatear(pedido.calcularTotal().total)
                            ),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = stringResource(R.string.pedido_pizzas, textoPizzas(pedido.items)),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = stringResource(R.string.pedido_entrega, pedido.direccionEntrega),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
