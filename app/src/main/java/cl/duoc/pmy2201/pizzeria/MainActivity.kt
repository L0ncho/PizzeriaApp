package cl.duoc.pmy2201.pizzeria

import android.content.res.AssetManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import cl.duoc.pmy2201.pizzeria.data.LectorCsv
import cl.duoc.pmy2201.pizzeria.model.Pedido
import cl.duoc.pmy2201.pizzeria.model.Producto
import cl.duoc.pmy2201.pizzeria.model.ResumenDia
import cl.duoc.pmy2201.pizzeria.service.ResultadoSolicitud
import cl.duoc.pmy2201.pizzeria.ui.PantallaInicio
import cl.duoc.pmy2201.pizzeria.ui.PedidosViewModel
import cl.duoc.pmy2201.pizzeria.ui.theme.PizzeriaAppTheme
import java.io.IOException
import java.io.InputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val assetManager: AssetManager = applicationContext.assets
        val lectorCsv: LectorCsv = LectorCsv(abrirArchivo = { nombre: String ->
            try {
                assetManager.open(nombre)
            } catch (excepcion: IOException) {
                null as InputStream?
            }
        })
        val pedidosViewModel: PedidosViewModel by viewModels { PedidosViewModel.factory(lectorCsv) }

        enableEdgeToEdge()
        setContent {
            val cargando: Boolean by pedidosViewModel.cargando.observeAsState(true)
            val resumen: ResumenDia by pedidosViewModel.resumen.observeAsState(
                ResumenDia(totalClientes = 0, totalPedidos = 0, nombreUltimoCliente = null)
            )
            val productos: List<Producto> by pedidosViewModel.productos.observeAsState(emptyList())
            val pedidos: List<Pedido> by pedidosViewModel.pedidos.observeAsState(emptyList())
            val erroresCarga: List<String> by pedidosViewModel.erroresCarga.observeAsState(emptyList())
            val ultimoResultado: ResultadoSolicitud? by pedidosViewModel.ultimoResultado.observeAsState()
            PizzeriaAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PantallaInicio(
                        cargando = cargando,
                        resumen = resumen,
                        productos = productos,
                        pedidos = pedidos,
                        erroresCarga = erroresCarga,
                        ultimoResultado = ultimoResultado,
                        onRegistrar = pedidosViewModel::registrarPedido,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
