package cl.duoc.pmy2201.pizzeria.data

import cl.duoc.pmy2201.pizzeria.model.PizzaDeLaCasa
import cl.duoc.pmy2201.pizzeria.model.Producto
import cl.duoc.pmy2201.pizzeria.model.esPromocionable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ProductoRepositoryCsvTest {

    private val productoRepository: ProductoRepositoryCsv = ProductoRepositoryCsv()

    private fun buscarJamonChampinon(): Producto? =
        productoRepository.obtenerTodos().find { producto: Producto -> producto.nombre == "Jamón Champiñón" }

    @Test
    fun catalogoTieneSeisProductosSinErroresDeCarga() {
        val productos: List<Producto> = productoRepository.obtenerTodos()

        assertEquals(6, productos.size)
        assertTrue(productoRepository.erroresDeCarga().isEmpty())
    }

    @Test
    fun jamonChampinonEsPizzaDeLaCasaPromocionable() {
        val producto: Producto? = buscarJamonChampinon()

        val pizza: PizzaDeLaCasa = assertIs<PizzaDeLaCasa>(assertNotNull(producto))
        assertEquals(11490.0, pizza.precio)
        assertTrue(esPromocionable(pizza))
    }

    @Test
    fun recetaDeJamonChampinonTieneCincoIngredientes() {
        val producto: Producto = assertNotNull(buscarJamonChampinon())

        assertEquals(
            mapOf(
                "Masa" to 1,
                "Salsa de tomate" to 150,
                "Queso mozzarella" to 250,
                "Jamón" to 100,
                "Champiñón" to 100
            ),
            producto.ingredientes
        )
    }
}
