package cl.duoc.pmy2201.pizzeria.ui

import cl.duoc.pmy2201.pizzeria.model.PizzaClasica
import cl.duoc.pmy2201.pizzeria.model.PizzaEspecial
import kotlin.test.Test
import kotlin.test.assertEquals

class CartaDisponibleTest {

    private val receta: Map<String, Int> = mapOf("Masa" to 1, "Queso mozzarella" to 150)

    @Test
    fun pizzaPromocionableLlevaEstrella() {
        val pizza: PizzaClasica = PizzaClasica(nombre = "Pepperoni", precio = 11990.0, ingredientes = receta)

        assertEquals("Pepperoni - \$11.990 ★", textoProductoCarta(pizza))
    }

    @Test
    fun pizzaNoPromocionableNoLlevaEstrella() {
        val pizza: PizzaEspecial = PizzaEspecial(nombre = "Cuatro Quesos", precio = 13990.0, ingredientes = receta)

        assertEquals("Cuatro Quesos - \$13.990", textoProductoCarta(pizza))
    }
}
