package cl.duoc.pmy2201.pizzeria.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cl.duoc.pmy2201.pizzeria.R

/**
 * Muestra los errores encontrados al cargar los datos, uno por línea bajo un título.
 * Si [errores] está vacío no muestra nada.
 */
@Composable
fun AvisosCarga(errores: List<String>, modifier: Modifier = Modifier) {
    if (errores.isEmpty()) return

    Column(modifier = modifier) {
        Text(text = stringResource(R.string.titulo_avisos), style = MaterialTheme.typography.titleMedium)
        errores.forEach { error: String ->
            Text(text = error, style = MaterialTheme.typography.bodySmall)
        }
    }
}
