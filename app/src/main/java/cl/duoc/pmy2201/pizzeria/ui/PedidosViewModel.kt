package cl.duoc.pmy2201.pizzeria.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.pmy2201.pizzeria.data.InventarioRepository
import cl.duoc.pmy2201.pizzeria.data.InventarioRepositoryCsv
import cl.duoc.pmy2201.pizzeria.data.LectorCsv
import cl.duoc.pmy2201.pizzeria.data.ProductoRepository
import cl.duoc.pmy2201.pizzeria.data.ProductoRepositoryCsv
import cl.duoc.pmy2201.pizzeria.data.RepartidorRepository
import cl.duoc.pmy2201.pizzeria.data.RepartidorRepositoryCsv
import cl.duoc.pmy2201.pizzeria.model.Pedido
import cl.duoc.pmy2201.pizzeria.model.Producto
import cl.duoc.pmy2201.pizzeria.model.ResumenDia
import cl.duoc.pmy2201.pizzeria.service.AsignacionRepartidorService
import cl.duoc.pmy2201.pizzeria.service.CampoPedido
import cl.duoc.pmy2201.pizzeria.service.ClienteService
import cl.duoc.pmy2201.pizzeria.service.InventarioService
import cl.duoc.pmy2201.pizzeria.service.PedidoService
import cl.duoc.pmy2201.pizzeria.service.RegistroPedidoService
import cl.duoc.pmy2201.pizzeria.service.ResultadoSolicitud
import cl.duoc.pmy2201.pizzeria.service.SolicitudPedido
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Estado de la pantalla de pedidos: carta, errores de carga, pedidos del día y su resumen.
 *
 * Los repositorios CSV leen sus archivos de forma perezosa la primera vez que se consultan. Esa lectura
 * es de entrada/salida y puede tardar, por eso se fuerza en [dispatcherCarga] (de fondo) y no bloquea
 * el hilo principal. El registro de pedidos, en cambio, trabaja solo con datos ya cargados en memoria,
 * así que se hace directamente en el hilo principal.
 *
 * @param dispatcherCarga Dispatcher donde se cargan los repositorios; por defecto [Dispatchers.IO].
 * Se recibe por parámetro para poder reemplazarlo en las pruebas.
 */
class PedidosViewModel(
    private val productoRepository: ProductoRepository,
    private val inventarioRepository: InventarioRepository,
    private val repartidorRepository: RepartidorRepository,
    private val dispatcherCarga: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val clienteService: ClienteService = ClienteService()
    private val inventarioService: InventarioService = InventarioService(inventarioRepository)
    private val pedidoService: PedidoService = PedidoService(
        inventarioService = inventarioService,
        asignacionRepartidorService = AsignacionRepartidorService(repartidorRepository)
    )
    private val registroPedidoService: RegistroPedidoService = RegistroPedidoService(
        clienteService = clienteService,
        pedidoService = pedidoService
    )

    private val _cargando: MutableLiveData<Boolean> = MutableLiveData(true)
    private val _productos: MutableLiveData<List<Producto>> = MutableLiveData(emptyList())
    private val _erroresCarga: MutableLiveData<List<String>> = MutableLiveData(emptyList())
    private val _resumen: MutableLiveData<ResumenDia> = MutableLiveData(
        ResumenDia(totalClientes = 0, totalPedidos = 0, nombreUltimoCliente = null)
    )
    private val _pedidos: MutableLiveData<List<Pedido>> = MutableLiveData(emptyList())
    private val _ultimoResultado: MutableLiveData<ResultadoSolicitud?> = MutableLiveData(null)
    private val _disponibilidad: MutableLiveData<Map<Producto, Boolean>> = MutableLiveData(emptyMap())

    /** true mientras se cargan los repositorios; los pedidos no se registran hasta que termine. */
    val cargando: LiveData<Boolean> = _cargando

    /** Productos de la carta cargados correctamente. */
    val productos: LiveData<List<Producto>> = _productos

    /** Errores encontrados al cargar productos, inventario y repartidores. */
    val erroresCarga: LiveData<List<String>> = _erroresCarga

    /** Resumen del día: clientes, pedidos y nombre del último cliente registrado. */
    val resumen: LiveData<ResumenDia> = _resumen

    /** Pedidos registrados en el día, en orden de registro. */
    val pedidos: LiveData<List<Pedido>> = _pedidos

    /** Resultado del último intento de registrar un pedido, o null si aún no hay intentos. */
    val ultimoResultado: LiveData<ResultadoSolicitud?> = _ultimoResultado

    /**
     * Indica, para cada producto de la carta, si el inventario alcanza para preparar al menos una unidad.
     * Se calcula al terminar la carga y se recalcula después de cada intento de registro.
     */
    val disponibilidad: LiveData<Map<Producto, Boolean>> = _disponibilidad

    init {
        viewModelScope.launch {
            val carga: CargaInicial = withContext(dispatcherCarga) {
                val productosCargados: List<Producto> = productoRepository.obtenerTodos()
                inventarioRepository.obtenerInventario()
                repartidorRepository.obtenerTodos()
                val errores: List<String> = productoRepository.erroresDeCarga() +
                    inventarioRepository.erroresDeCarga() +
                    repartidorRepository.erroresDeCarga()
                CargaInicial(
                    productos = productosCargados,
                    errores = errores,
                    disponibilidad = calcularDisponibilidad(productosCargados)
                )
            }
            _productos.value = carga.productos
            _erroresCarga.value = carga.errores
            _disponibilidad.value = carga.disponibilidad
            _cargando.value = false
        }
    }

    /**
     * Valida solo los datos del cliente con [RegistroPedidoService.validarCliente] y retorna los campos inválidos.
     * Permite revisar al cliente antes de pasar a elegir la pizza; no registra nada.
     */
    fun validarCliente(nombre: String, telefono: String, correo: String, direccion: String): Set<CampoPedido> =
        registroPedidoService.validarCliente(nombre = nombre, telefono = telefono, correo = correo, direccion = direccion)

    /**
     * Intenta registrar el pedido de [solicitud] y publica el resultado en [ultimoResultado],
     * junto con el [resumen], los [pedidos] y la [disponibilidad] actualizados.
     * No hace nada mientras [cargando] sea true, porque el inventario y los repartidores aún no están listos.
     * Se ejecuta en el hilo principal porque solo usa datos en memoria.
     */
    fun registrarPedido(solicitud: SolicitudPedido) {
        if (_cargando.value == true) return

        _ultimoResultado.value = registroPedidoService.registrar(solicitud)
        _resumen.value = registroPedidoService.resumen()
        _pedidos.value = pedidoService.pedidosDelDia().toList()
        _disponibilidad.value = calcularDisponibilidad(_productos.value ?: emptyList())
    }

    /** Calcula con [InventarioService.puedePreparar] si cada producto de [productos] se puede preparar. */
    private fun calcularDisponibilidad(productos: List<Producto>): Map<Producto, Boolean> =
        productos.associateWith { producto: Producto -> inventarioService.puedePreparar(producto) }

    /** Datos que se obtienen en el dispatcher de carga y luego se publican en el hilo principal. */
    private data class CargaInicial(
        val productos: List<Producto>,
        val errores: List<String>,
        val disponibilidad: Map<Producto, Boolean>
    )

    companion object {

        /**
         * Crea la fábrica del [PedidosViewModel] con los repositorios CSV, que leen sus archivos con [lectorCsv].
         */
        fun factory(lectorCsv: LectorCsv): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PedidosViewModel(
                    productoRepository = ProductoRepositoryCsv(lectorCsv),
                    inventarioRepository = InventarioRepositoryCsv(lectorCsv),
                    repartidorRepository = RepartidorRepositoryCsv(lectorCsv)
                )
            }
        }
    }
}
