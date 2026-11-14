package cl.duoc.pmy2201.pizzeria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.pmy2201.pizzeria.data.LectorCsv
import cl.duoc.pmy2201.pizzeria.data.ProductoRepositoryCsv
import cl.duoc.pmy2201.pizzeria.model.Producto
import cl.duoc.pmy2201.pizzeria.ui.CartaDisponible
import cl.duoc.pmy2201.pizzeria.ui.theme.PizzeriaAppTheme
import java.io.IOException
import java.io.InputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lectorCsv: LectorCsv = LectorCsv(abrirArchivo = { nombre: String ->
            try {
                assets.open(nombre)
            } catch (excepcion: IOException) {
                null as InputStream?
            }
        })
        val productoRepository: ProductoRepositoryCsv = ProductoRepositoryCsv(lectorCsv)
        val productos: List<Producto> = productoRepository.obtenerTodos()
        val errores: List<String> = productoRepository.erroresDeCarga()

        enableEdgeToEdge()
        setContent {
            PizzeriaAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Greeting()
                        CartaDisponible(productos = productos, errores = errores)
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.mensaje_bienvenida),
        style = MaterialTheme.typography.headlineSmall,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PizzeriaAppTheme {
        Greeting()
    }
}
