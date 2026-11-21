package cl.duoc.pmy2201.pizzeria.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.pmy2201.pizzeria.model.Pedido
import cl.duoc.pmy2201.pizzeria.model.Producto
import cl.duoc.pmy2201.pizzeria.model.ResumenDia
import cl.duoc.pmy2201.pizzeria.service.ResultadoSolicitud
import cl.duoc.pmy2201.pizzeria.service.SolicitudPedido
import cl.duoc.pmy2201.pizzeria.ui.theme.PizzeriaAppTheme

/**
 * Pantalla inicial: bienvenida, resumen del día, formulario de pedido, pedidos registrados,
 * carta disponible y avisos de carga, en ese orden y con desplazamiento vertical.
 *
 * No tiene estado propio del negocio: recibe los datos ya observados y avisa con [onRegistrar]
 * cuando el operador envía el formulario, para poder mostrarla en vistas previas sin ViewModel.
 */
@Composable
fun PantallaInicio(
    cargando: Boolean,
    resumen: ResumenDia,
    productos: List<Producto>,
    pedidos: List<Pedido>,
    erroresCarga: List<String>,
    ultimoResultado: ResultadoSolicitud?,
    onRegistrar: (SolicitudPedido) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Greeting() }
        item { ResumenDiaCard(resumen = resumen, cargando = cargando) }
        item {
            FormularioPedido(
                productos = productos,
                cargando = cargando,
                ultimoResultado = ultimoResultado,
                onRegistrar = onRegistrar
            )
        }
        item { ListaPedidos(pedidos = pedidos) }
        item { CartaDisponible(productos = productos) }
        item { AvisosCarga(errores = erroresCarga) }
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
            pedidos = emptyList(),
            erroresCarga = emptyList(),
            ultimoResultado = null,
            onRegistrar = { _: SolicitudPedido -> }
        )
    }
}
