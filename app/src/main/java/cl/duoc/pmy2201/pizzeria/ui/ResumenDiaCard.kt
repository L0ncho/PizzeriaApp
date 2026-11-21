package cl.duoc.pmy2201.pizzeria.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cl.duoc.pmy2201.pizzeria.R
import cl.duoc.pmy2201.pizzeria.model.ResumenDia

/**
 * Tarjeta con el resumen del día en tres recuadros del mismo ancho: clientes registrados y pedidos realizados
 * (sobre primaryContainer, con el número grande) y el último cliente (sobre secondaryContainer, o
 * "Sin registros" si aún no hay clientes). Mientras [cargando] es true muestra el aviso de carga en su lugar.
 */
@Composable
fun ResumenDiaCard(resumen: ResumenDia, cargando: Boolean, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = stringResource(R.string.titulo_resumen), style = MaterialTheme.typography.titleMedium)
            if (cargando) {
                Text(text = stringResource(R.string.cargando), style = MaterialTheme.typography.bodyLarge)
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RecuadroResumen(
                        valor = resumen.totalClientes.toString(),
                        etiqueta = stringResource(R.string.etiqueta_clientes),
                        estiloValor = MaterialTheme.typography.headlineMedium,
                        fondo = MaterialTheme.colorScheme.primaryContainer,
                        contenido = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    RecuadroResumen(
                        valor = resumen.totalPedidos.toString(),
                        etiqueta = stringResource(R.string.etiqueta_pedidos),
                        estiloValor = MaterialTheme.typography.headlineMedium,
                        fondo = MaterialTheme.colorScheme.primaryContainer,
                        contenido = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    RecuadroResumen(
                        valor = resumen.nombreUltimoCliente ?: stringResource(R.string.sin_registros),
                        etiqueta = stringResource(R.string.etiqueta_ultimo_cliente),
                        estiloValor = MaterialTheme.typography.titleSmall,
                        fondo = MaterialTheme.colorScheme.secondaryContainer,
                        contenido = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Recuadro del resumen con un [valor] destacado y su [etiqueta] debajo, sobre el color [fondo].
 * Ocupa todo el alto de la fila para que los tres recuadros queden parejos.
 */
@Composable
private fun RecuadroResumen(
    valor: String,
    etiqueta: String,
    estiloValor: TextStyle,
    fondo: Color,
    contenido: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = fondo,
        contentColor = contenido,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxHeight()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = valor, style = estiloValor, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(text = etiqueta, style = MaterialTheme.typography.bodySmall)
        }
    }
}
