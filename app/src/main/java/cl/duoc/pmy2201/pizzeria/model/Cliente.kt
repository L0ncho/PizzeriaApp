package cl.duoc.pmy2201.pizzeria.model

import cl.duoc.pmy2201.pizzeria.util.normalizarTelefono

/**
 * Cliente que realiza el pedido.
 *
 * Se identifica por su nombre y su correo; si no tiene correo, por su nombre y su teléfono
 * (ver [equals]).
 *
 * @param nombre Nombre del cliente.
 * @param telefono Teléfono de contacto.
 * @property correo Correo electrónico, o `null` si no se informó.
 * @property direccion Dirección de entrega.
 */
class Cliente(
    nombre: String,
    telefono: String,
    val correo: String?,
    val direccion: String
) : Usuario(nombre, telefono) {

    override fun describir(): String =
        "${super.describir()} - Correo: ${correo ?: "Sin correo"} - Dirección: $direccion"

    /**
     * Dos clientes son iguales si tienen el mismo nombre (sin distinguir mayúsculas ni espacios en los extremos) y:
     * - si tienen [correo], el mismo correo (sin distinguir mayúsculas); el teléfono no se compara;
     * - si no tienen correo (`null`), el mismo teléfono normalizado con
     *   [cl.duoc.pmy2201.pizzeria.util.normalizarTelefono] (o sin espacios, si no tiene un formato válido).
     * Un cliente con correo y otro sin correo nunca son iguales, aunque coincidan nombre y teléfono.
     * La dirección nunca se compara.
     * El operador `==` de Kotlin llama a este método, por lo que esta implementación define
     * cómo se comparan los clientes con `==`.
     * Usa `is Cliente` para que también funcione con futuras subclases.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is Cliente && claveIdentidad() == other.claveIdentidad()
    }

    /**
     * Calcula el hash con la misma clave de identidad que usa [equals] (nombre normalizado y correo,
     * o teléfono si no hay correo), de modo que dos clientes iguales tengan el mismo hash
     * (necesario para usarlos en `Set` o como claves de `Map`).
     */
    override fun hashCode(): Int = claveIdentidad().hashCode()

    /**
     * Funciones de desestructuración: permiten escribir `val (nombre, correo, telefono) = cliente`.
     * [component1] retorna el nombre, [component2] el correo y [component3] el teléfono.
     */
    operator fun component1(): String = nombre

    /** Retorna el [correo] del cliente (ver [component1]). */
    operator fun component2(): String? = correo

    /** Retorna el teléfono del cliente (ver [component1]). */
    operator fun component3(): String = telefono

    private fun nombreNormalizado(): String = nombre.trim().lowercase()

    private fun correoNormalizado(): String? = correo?.lowercase()

    private fun telefonoNormalizado(): String = normalizarTelefono(telefono) ?: telefono.replace(" ", "")

    /**
     * Retorna el nombre normalizado junto a `"correo:<correo normalizado>"` si hay correo,
     * o `"telefono:<teléfono normalizado>"` si no lo hay. Los prefijos evitan que un correo
     * y un teléfono con el mismo texto se consideren iguales.
     */
    private fun claveIdentidad(): Pair<String, String> {
        val correoActual: String? = correoNormalizado()
        val contacto: String = if (correoActual != null) "correo:$correoActual" else "telefono:${telefonoNormalizado()}"
        return Pair(nombreNormalizado(), contacto)
    }
}
