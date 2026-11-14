package cl.duoc.pmy2201.pizzeria.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cl.duoc.pmy2201.pizzeria.R
import cl.duoc.pmy2201.pizzeria.model.Producto
import cl.duoc.pmy2201.pizzeria.model.esPromocionable
import cl.duoc.pmy2201.pizzeria.util.FormatoMoneda

/**
 * Devuelve el texto de [producto] para la carta: "nombre - precio", con el precio
 * formateado por [FormatoMoneda] y " ★" al final si el producto es promocionable.
 *
 * Ejemplo: "Pepperoni - $11.990 ★".
 */
fun textoProductoCarta(producto: Producto): String {
    val texto: String = "${producto.nombre} - ${FormatoMoneda.formatear(producto.precio)}"
    return if (esPromocionable(producto)) "$texto ★" else texto
}

/**
 * Muestra la carta disponible: el título, un texto por cada producto
 * (ver [textoProductoCarta]) y la leyenda de promocionables.
 *
 * Si [productos] está vacío muestra un aviso de carta vacía, y si hay [errores]
 * de carga los muestra uno por línea al final.
 */
@Composable
fun CartaDisponible(productos: List<Producto>, errores: List<String>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.titulo_carta),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
        if (productos.isEmpty()) {
            Text(text = stringResource(R.string.carta_vacia), style = MaterialTheme.typography.bodyLarge)
        } else {
            productos.forEach { producto: Producto ->
                Text(text = textoProductoCarta(producto), style = MaterialTheme.typography.bodyLarge)
            }
            Text(text = stringResource(R.string.leyenda_promocionable), style = MaterialTheme.typography.bodySmall)
        }
        errores.forEach { error: String ->
            Text(text = error, style = MaterialTheme.typography.bodySmall)
        }
    }
}
