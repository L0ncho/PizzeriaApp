package cl.duoc.pmy2201.pizzeria.ui

import cl.duoc.pmy2201.pizzeria.model.ItemPedido
import cl.duoc.pmy2201.pizzeria.model.PizzaClasica
import kotlin.test.Test
import kotlin.test.assertEquals

class ListaPedidosTest {

    private val pepperoni: PizzaClasica = PizzaClasica(
        nombre = "Pepperoni",
        precio = 11990.0,
        ingredientes = mapOf("Masa" to 1, "Pepperoni" to 80)
    )

    private val napolitana: PizzaClasica = PizzaClasica(
        nombre = "Napolitana",
        precio = 10990.0,
        ingredientes = mapOf("Masa" to 1, "Tomate" to 150)
    )

    @Test
    fun textoPizzasConUnItemMuestraCantidadYNombre() {
        val texto: String = textoPizzas(listOf(ItemPedido(producto = pepperoni, cantidad = 2)))

        assertEquals("2 x Pepperoni", texto)
    }

    @Test
    fun textoPizzasConDosItemsLosSeparaConComaEnSuOrden() {
        val texto: String = textoPizzas(
            listOf(
                ItemPedido(producto = pepperoni, cantidad = 2),
                ItemPedido(producto = napolitana, cantidad = 1)
            )
        )

        assertEquals("2 x Pepperoni, 1 x Napolitana", texto)
    }
}
