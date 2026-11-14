package cl.duoc.pmy2201.pizzeria.data

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream

/**
 * Lee archivos CSV (UTF-8, separador `;`, con encabezado).
 *
 * La fuente de los archivos se recibe por parámetro en [abrirArchivo]: por defecto
 * se abren desde los resources del classpath; en Android se pueden abrir desde assets.
 *
 * @param abrirArchivo función que recibe el nombre del archivo y devuelve su flujo,
 * o `null` si no existe.
 */
class LectorCsv(
    private val abrirArchivo: (String) -> InputStream? = { nombre: String ->
        LectorCsv::class.java.classLoader.getResourceAsStream(nombre)
    }
) {

    /**
     * Lee [nombreArchivo] usando la fuente recibida en [abrirArchivo]
     * (por defecto, resources del classpath; en Android, assets).
     *
     * Quita el BOM si existe, descarta las líneas vacías, salta el encabezado
     * y separa cada línea por `;` quitando espacios de cada valor.
     * No lanza excepciones: los problemas se informan en [ResultadoLectura.errores].
     */
    fun leer(nombreArchivo: String): ResultadoLectura {
        val flujo: InputStream? = abrirArchivo(nombreArchivo)

        return if (flujo == null) {
            ResultadoLectura(
                filas = emptyList(),
                errores = listOf("No se encontró el archivo '$nombreArchivo'.")
            )
        } else {
            try {
                val contenido: String = flujo.bufferedReader(Charsets.UTF_8).use { lector: BufferedReader -> lector.readText() }
                val filas: List<List<String>> = contenido
                    .removePrefix(BOM)
                    .lines()
                    .filter { linea: String -> linea.isNotBlank() }
                    .drop(1)
                    .map { linea: String -> linea.split(SEPARADOR).map { valor: String -> valor.trim() } }
                ResultadoLectura(filas = filas, errores = emptyList())
            } catch (excepcion: IOException) {
                ResultadoLectura(
                    filas = emptyList(),
                    errores = listOf("Error al leer el archivo '$nombreArchivo': ${excepcion.message}")
                )
            }
        }
    }

    private companion object {
        const val BOM: String = "\uFEFF"
        const val SEPARADOR: String = ";"
    }
}
