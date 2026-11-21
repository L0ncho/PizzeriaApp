package cl.duoc.pmy2201.pizzeria.util

/** Rango permitido de pizzas, tanto por producto como para el total del pedido. */
val RANGO_CANTIDAD_PIZZAS: IntRange = 1..100

private const val PREFIJO_CHILE: String = "+56"
private val FORMATO_TELEFONO: Regex = Regex("""^(?:\+?56)?(\d{9})$""")
private val FORMATO_CORREO: Regex = Regex("""^[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,}$""")
private val FORMATO_TELEFONO_NORMALIZADO: Regex = Regex("""^\+56(\d)(\d{4})(\d{4})$""")
private val FORMATO_TELEFONO_INTERNACIONAL: Regex = Regex("""^\+(?!56)(\d{2})(\d{10})$""")
private val FORMATO_INTERNACIONAL_NORMALIZADO: Regex = Regex("""^\+(?!56)(\d{2})(\d{3})(\d{3})(\d{4})$""")

/**
 * Normaliza un teléfono, sin considerar espacios:
 * - chileno de 9 dígitos, con o sin prefijo +56 o 56, al formato +56XXXXXXXXX;
 * - internacional con + y código de país de 2 dígitos distinto de 56, seguido de 10 dígitos, al formato +XXXXXXXXXXXX.
 * Retorna null si [texto] no tiene ninguno de esos formatos.
 */
fun normalizarTelefono(texto: String): String? {
    val sinEspacios: String = texto.replace(" ", "")
    val coincidenciaChile: MatchResult? = FORMATO_TELEFONO.matchEntire(sinEspacios)
    if (coincidenciaChile != null) return "$PREFIJO_CHILE${coincidenciaChile.groupValues[1]}"

    val coincidenciaInternacional: MatchResult? = FORMATO_TELEFONO_INTERNACIONAL.matchEntire(sinEspacios)
    return coincidenciaInternacional?.let { resultado: MatchResult ->
        val (codigoPais: String, numero: String) = resultado.destructured
        "+$codigoPais$numero"
    }
}

/**
 * Da formato a un teléfono normalizado:
 * - chileno (+56 seguido de 9 dígitos) como `+56 (X) XXXX-XXXX`, por ejemplo `"+56931717552"` → `"+56 (9) 3171-7552"`;
 * - internacional (+ y código de 2 dígitos seguido de 10 dígitos) como `+XX (XXX) XXX-XXXX`,
 *   por ejemplo `"+541123456789"` → `"+54 (112) 345-6789"`.
 * Si [telefono] no tiene ninguno de esos formatos, lo retorna sin cambios.
 */
fun formatearTelefono(telefono: String): String {
    val coincidenciaChile: MatchResult? = FORMATO_TELEFONO_NORMALIZADO.matchEntire(telefono)
    if (coincidenciaChile != null) {
        val (bloque1: String, bloque2: String, bloque3: String) = coincidenciaChile.destructured
        return "$PREFIJO_CHILE ($bloque1) $bloque2-$bloque3"
    }

    val coincidenciaInternacional: MatchResult? = FORMATO_INTERNACIONAL_NORMALIZADO.matchEntire(telefono)
    return if (coincidenciaInternacional != null) {
        val (codigoPais: String, bloque1: String, bloque2: String, bloque3: String) = coincidenciaInternacional.destructured
        "+$codigoPais ($bloque1) $bloque2-$bloque3"
    } else {
        telefono
    }
}

/**
 * Indica si [texto] tiene el formato usuario@dominio.ext (sin considerar espacios ni mayúsculas).
 */
fun esCorreoValido(texto: String): Boolean = FORMATO_CORREO.matches(texto.trim().lowercase())

/**
 * Indica si [cantidad] está dentro de [RANGO_CANTIDAD_PIZZAS].
 */
fun esCantidadValida(cantidad: Int): Boolean = cantidad in RANGO_CANTIDAD_PIZZAS
