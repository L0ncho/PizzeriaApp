package cl.duoc.pmy2201.pizzeria.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import cl.duoc.pmy2201.pizzeria.R
import cl.duoc.pmy2201.pizzeria.ui.theme.PizzeriaAppTheme

/** Mensaje de bienvenida que encabeza la pantalla inicial. */
@Composable
fun Greeting(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.mensaje_bienvenida),
        style = MaterialTheme.typography.headlineSmall,
        modifier = modifier
    )
}

/** Vista previa de [Greeting] con el tema de la app. */
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PizzeriaAppTheme {
        Greeting()
    }
}
