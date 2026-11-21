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
import cl.duoc.pmy2201.pizzeria.model.ResumenDia

/**
 * Tarjeta con el resumen del día: clientes registrados, pedidos realizados y último cliente.
 * Si no hay clientes muestra un aviso en lugar del último cliente, y mientras [cargando] es true
 * muestra el aviso de carga en lugar de los datos.
 */
@Composable
fun ResumenDiaCard(resumen: ResumenDia, cargando: Boolean, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = stringResource(R.string.titulo_resumen), style = MaterialTheme.typography.titleMedium)
            if (cargando) {
                Text(text = stringResource(R.string.cargando), style = MaterialTheme.typography.bodyLarge)
            } else {
                Text(
                    text = stringResource(R.string.resumen_clientes, resumen.totalClientes),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(R.string.resumen_pedidos, resumen.totalPedidos),
                    style = MaterialTheme.typography.bodyLarge
                )
                val nombreUltimo: String? = resumen.nombreUltimoCliente
                Text(
                    text = if (nombreUltimo != null) {
                        stringResource(R.string.resumen_ultimo_cliente, nombreUltimo)
                    } else {
                        stringResource(R.string.resumen_sin_clientes)
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
