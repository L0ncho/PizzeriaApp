package cl.duoc.pmy2201.pizzeria.service

import cl.duoc.pmy2201.pizzeria.model.Cliente

/**
 * Mantiene los clientes registrados en el día, sin duplicados.
 * Los guarda en un [MutableSet], que usa `equals` y `hashCode` de [Cliente] para no aceptar
 * dos clientes iguales (mismo nombre y mismo correo o, si no tienen correo, mismo nombre y mismo teléfono;
 * ver [Cliente.equals]).
 */
class ClienteService {

    private val clientes: MutableSet<Cliente> = mutableSetOf()

    /**
     * Retorna el cliente registrado que es igual (==) a uno con [nombre], [correo] y [telefono], o null si no existe.
     * Si [correo] no es null, se compara por nombre y correo y [telefono] no influye;
     * si es null, se compara por nombre y teléfono (ver [Cliente.equals]). La dirección nunca influye.
     */
    fun buscar(nombre: String, correo: String?, telefono: String): Cliente? {
        val buscado: Cliente = Cliente(nombre = nombre, telefono = telefono, correo = correo, direccion = "")
        return clientes.find { cliente: Cliente -> cliente == buscado }
    }

    /**
     * Registra [cliente]. Retorna false si ya existía un cliente igual, en cuyo caso no se agrega.
     */
    fun registrar(cliente: Cliente): Boolean = clientes.add(cliente)

    /** Retorna una copia de los clientes registrados en el día. */
    fun clientesDelDia(): Set<Cliente> = clientes.toSet()

    /**
     * Retorna el último cliente agregado con [registrar], o null si no hay clientes.
     * [mutableSetOf] mantiene el orden de inserción, y registrar un cliente que ya existía no lo agrega
     * de nuevo, por lo que un cliente repetido no cambia el último.
     */
    fun ultimoRegistrado(): Cliente? = clientes.lastOrNull()
}
