package cl.duoc.pmy2201.pizzeria.data

import java.io.ByteArrayInputStream
import java.io.InputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LectorCsvTest {

    @Test
    fun lectorConFuenteInyectadaLeeFilasSinEncabezado() {
        val contenido: String = "nombre;precio\nPepperoni;11990\n\nNapolitana;10990\n"
        val lector: LectorCsv = LectorCsv(abrirArchivo = { _: String ->
            ByteArrayInputStream(contenido.toByteArray(Charsets.UTF_8))
        })

        val resultado: ResultadoLectura = lector.leer("productos.csv")

        assertEquals(2, resultado.filas.size)
        assertEquals(listOf("Pepperoni", "11990"), resultado.filas[0])
        assertEquals(listOf("Napolitana", "10990"), resultado.filas[1])
        assertTrue(resultado.errores.isEmpty())
    }

    @Test
    fun archivoInexistenteInformaError() {
        val nombreArchivo: String = "no_existe.csv"
        val lector: LectorCsv = LectorCsv(abrirArchivo = { _: String -> null as InputStream? })

        val resultado: ResultadoLectura = lector.leer(nombreArchivo)

        assertEquals(0, resultado.filas.size)
        assertEquals(1, resultado.errores.size)
        assertTrue(resultado.errores[0].contains(nombreArchivo))
    }
}
