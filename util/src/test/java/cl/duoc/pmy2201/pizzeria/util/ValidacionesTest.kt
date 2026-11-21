package cl.duoc.pmy2201.pizzeria.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ValidacionesTest {

    @Test
    fun celularSeFormateaConPrimerDigitoEntreParentesis() {
        val formateado: String = formatearTelefono("+56931717552")

        assertEquals("+56 (9) 3171-7552", formateado)
    }

    @Test
    fun fijoSeFormateaConPrimerDigitoEntreParentesis() {
        val formateado: String = formatearTelefono("+56223456789")

        assertEquals("+56 (2) 2345-6789", formateado)
    }

    @Test
    fun textoQueNoCalzaConElPatronSeRetornaSinCambios() {
        val formateado: String = formatearTelefono("12345")

        assertEquals("12345", formateado)
    }

    @Test
    fun internacionalSeFormateaConFormatoGeneral() {
        val formateado: String = formatearTelefono("+541123456789")

        assertEquals("+54 (112) 345-6789", formateado)
    }

    @Test
    fun internacionalConEspaciosSeNormaliza() {
        val normalizado: String? = normalizarTelefono("+54 1123456789")

        assertEquals("+541123456789", normalizado)
    }

    @Test
    fun chilenoSigueNormalizandoseConPrefijo() {
        val sinPrefijo: String? = normalizarTelefono("931717552")
        val conPrefijoSinSignoMas: String? = normalizarTelefono("56931717552")

        assertEquals("+56931717552", sinPrefijo)
        assertEquals("+56931717552", conPrefijoSinSignoMas)
    }

    @Test
    fun internacionalSinSignoMasEsInvalido() {
        val normalizado: String? = normalizarTelefono("541123456789")

        assertNull(normalizado)
    }

    @Test
    fun prefijoChilenoConDiezDigitosEsInvalido() {
        val normalizado: String? = normalizarTelefono("+561123456789")

        assertNull(normalizado)
    }

    @Test
    fun codigoDeUnDigitoEsInvalido() {
        val normalizado: String? = normalizarTelefono("+11234567890")

        assertNull(normalizado)
    }

    @Test
    fun limitesDelRangoSonCantidadesValidas() {
        val minimoEsValido: Boolean = esCantidadValida(1)
        val maximoEsValido: Boolean = esCantidadValida(100)

        assertTrue(minimoEsValido)
        assertTrue(maximoEsValido)
    }

    @Test
    fun valoresFueraDelRangoNoSonCantidadesValidas() {
        val ceroEsValido: Boolean = esCantidadValida(0)
        val cientoUnoEsValido: Boolean = esCantidadValida(101)

        assertFalse(ceroEsValido)
        assertFalse(cientoUnoEsValido)
    }

    @Test
    fun nombresConLetrasTildesYEnieSonValidos() {
        val anaEsValido: Boolean = esNombreValido("Ana")
        val joseEsValido: Boolean = esNombreValido("José Ñúñez")
        val matiasEsValido: Boolean = esNombreValido("Matías Agüero")

        assertTrue(anaEsValido)
        assertTrue(joseEsValido)
        assertTrue(matiasEsValido)
    }

    @Test
    fun nombresCortosConNumerosOEnBlancoNoSonValidos() {
        val unaLetraEsValido: Boolean = esNombreValido("A")
        val conNumeroEsValido: Boolean = esNombreValido("Ana2")
        val enBlancoEsValido: Boolean = esNombreValido("   ")

        assertFalse(unaLetraEsValido)
        assertFalse(conNumeroEsValido)
        assertFalse(enBlancoEsValido)
    }

    @Test
    fun direccionConCincoCaracteresYUnaLetraEsValida() {
        val esValida: Boolean = esDireccionValida("Calle 1")

        assertTrue(esValida)
    }

    @Test
    fun direccionCortaSoloNumerosOEnBlancoNoEsValida() {
        val cortaEsValida: Boolean = esDireccionValida("Av 1")
        val soloNumerosEsValida: Boolean = esDireccionValida("12345")
        val enBlancoEsValida: Boolean = esDireccionValida("   ")

        assertFalse(cortaEsValida)
        assertFalse(soloNumerosEsValida)
        assertFalse(enBlancoEsValida)
    }
}
