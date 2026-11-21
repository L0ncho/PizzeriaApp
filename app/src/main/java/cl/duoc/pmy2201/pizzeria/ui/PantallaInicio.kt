package cl.duoc.pmy2201.pizzeria.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.pmy2201.pizzeria.R
import cl.duoc.pmy2201.pizzeria.model.Pedido
import cl.duoc.pmy2201.pizzeria.model.Producto
import cl.duoc.pmy2201.pizzeria.model.ResumenDia
import cl.duoc.pmy2201.pizzeria.service.CampoPedido
import cl.duoc.pmy2201.pizzeria.service.ResultadoSolicitud
import cl.duoc.pmy2201.pizzeria.service.SolicitudPedido
import cl.duoc.pmy2201.pizzeria.ui.theme.PizzeriaAppTheme

/**
 * Pantalla inicial: barra superior con la bienvenida y, con desplazamiento vertical, el resumen del día,
 * el formulario de pedido, los pedidos registrados y los avisos de carga, en ese orden.
 *
 * No tiene estado propio del negocio: recibe los datos ya observados y avisa con [onValidarCliente]
 * y [onRegistrar] cuando el operador actúa, para poder mostrarla en vistas previas sin ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInicio(
    cargando: Boolean,
    resumen: ResumenDia,
    productos: List<Producto>,
    disponibilidad: Map<Producto, Boolean>,
    pedidos: List<Pedido>,
    erroresCarga: List<String>,
    ultimoResultado: ResultadoSolicitud?,
    onValidarCliente: (nombre: String, telefono: String, correo: String, direccion: String) -> Set<CampoPedido>,
    onRegistrar: (SolicitudPedido) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.mensaje_bienvenida),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding: PaddingValues ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { ResumenDiaCard(resumen = resumen, cargando = cargando) }
            item {
                FormularioPedido(
                    productos = productos,
                    disponibilidad = disponibilidad,
                    cargando = cargando,
                    ultimoResultado = ultimoResultado,
                    onValidarCliente = onValidarCliente,
                    onRegistrar = onRegistrar
                )
            }
            item { ListaPedidos(pedidos = pedidos) }
            item { AvisosCarga(errores = erroresCarga) }
        }
    }
}

/** Vista previa de [PantallaInicio] sin datos y con el resumen en 0. */
@Preview(showBackground = true)
@Composable
fun PantallaInicioPreview() {
    PizzeriaAppTheme {
        PantallaInicio(
            cargando = false,
            resumen = ResumenDia(totalClientes = 0, totalPedidos = 0, nombreUltimoCliente = null),
            productos = emptyList(),
            disponibilidad = emptyMap(),
            pedidos = emptyList(),
            erroresCarga = emptyList(),
            ultimoResultado = null,
            onValidarCliente = { _: String, _: String, _: String, _: String -> emptySet() },
            onRegistrar = { _: SolicitudPedido -> }
        )
    }
}
