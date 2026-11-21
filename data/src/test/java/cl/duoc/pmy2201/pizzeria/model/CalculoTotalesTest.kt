package cl.duoc.pmy2201.pizzeria.model

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CalculoTotalesTest {

    private val tolerancia: Double = 0.01

    private val fechaFueraDePromocion: LocalDate = LocalDate.of(2026, 11, 18)
    private val fechaEnPromocion: LocalDate = LocalDate.of(2026, 11, 4)

    private val pepperoni: PizzaClasica = PizzaClasica(
        nombre = "Pepperoni",
        precio = 11990.0,
        ingredientes = mapOf("Masa" to 1, "Pepperoni" to 80)
    )

    private val cuatroQuesos: PizzaEspecial = PizzaEspecial(
        nombre = "Cuatro Quesos",
        precio = 13990.0,
        ingredientes = mapOf("Queso mozzarella" to 150, "Queso azul" to 60)
    )

    private val jamonChampinon: PizzaDeLaCasa = PizzaDeLaCasa(
        nombre = "Jamón Champiñón",
        precio = 11490.0,
        ingredientes = mapOf("Masa" to 1, "Jamón" to 100)
    )

    @Test
    fun tresPizzasNoTienenDescuento() {
        val totales: TotalesPedido = calcularTotales(
            items = listOf(ItemPedido(producto = pepperoni, cantidad = 3)),
            fecha = fechaFueraDePromocion
        )

        assertEquals(35970.0, totales.subtotal, tolerancia)
        assertEquals(0.0, totales.descuentoCantidad, tolerancia)
        assertEquals(0.0, totales.descuentoPromocion, tolerancia)
        assertEquals(35970.0, totales.total, tolerancia)
    }

    @Test
    fun cuatroPepperoniTienenDescuentoDel10PorCiento() {
        val totales: TotalesPedido = calcularTotales(
            items = listOf(ItemPedido(producto = pepperoni, cantidad = 4)),
            fecha = fechaFueraDePromocion
        )

        assertEquals(47960.0, totales.subtotal, tolerancia)
        assertEquals(4796.0, totales.descuentoCantidad, tolerancia)
        assertEquals(0.0, totales.descuentoPromocion, tolerancia)
        assertEquals(43164.0, totales.total, tolerancia)
    }

    @Test
    fun cuatroPepperoniEnPeriodoDePromocionSumanAmbosDescuentos() {
        val totales: TotalesPedido = calcularTotales(
            items = listOf(ItemPedido(producto = pepperoni, cantidad = 4)),
            fecha = fechaEnPromocion
        )

        assertEquals(47960.0, totales.subtotal, tolerancia)
        assertEquals(4796.0, totales.descuentoCantidad, tolerancia)
        assertEquals(7194.0, totales.descuentoPromocion, tolerancia)
        assertEquals(35970.0, totales.total, tolerancia)
    }

    @Test
    fun pepperoniFueraDelPeriodoNoTienePromocion() {
        val totales: TotalesPedido = calcularTotales(
            items = listOf(ItemPedido(producto = pepperoni, cantidad = 1)),
            fecha = fechaFueraDePromocion
        )

        assertEquals(0.0, totales.descuentoPromocion, tolerancia)
        assertEquals(11990.0, totales.total, tolerancia)
    }

    @Test
    fun pizzaEspecialEnPeriodoNoTienePromocion() {
        val totales: TotalesPedido = calcularTotales(
            items = listOf(ItemPedido(producto = cuatroQuesos, cantidad = 1)),
            fecha = fechaEnPromocion
        )

        assertEquals(0.0, totales.descuentoPromocion, tolerancia)
        assertEquals(13990.0, totales.total, tolerancia)
    }

    @Test
    fun promocionSoloSeAplicaALasPizzasPromocionables() {
        val totales: TotalesPedido = calcularTotales(
            items = listOf(
                ItemPedido(producto = pepperoni, cantidad = 2),
                ItemPedido(producto = cuatroQuesos, cantidad = 1)
            ),
            fecha = fechaEnPromocion
        )

        assertEquals(37970.0, totales.subtotal, tolerancia)
        assertEquals(0.0, totales.descuentoCantidad, tolerancia)
        assertEquals(3597.0, totales.descuentoPromocion, tolerancia)
        assertEquals(34373.0, totales.total, tolerancia)
    }

    @Test
    fun pedidoMixtoConMasDeTresPizzasSumaAmbosDescuentos() {
        val totales: TotalesPedido = calcularTotales(
            items = listOf(
                ItemPedido(producto = pepperoni, cantidad = 1),
                ItemPedido(producto = cuatroQuesos, cantidad = 3)
            ),
            fecha = fechaEnPromocion
        )

        assertEquals(53960.0, totales.subtotal, tolerancia)
        assertEquals(5396.0, totales.descuentoCantidad, tolerancia)
        assertEquals(1798.5, totales.descuentoPromocion, tolerancia)
        assertEquals(46765.5, totales.total, tolerancia)
    }

    @Test
    fun ambasClasesPromocionablesSumanAlDescuento() {
        val totales: TotalesPedido = calcularTotales(
            items = listOf(
                ItemPedido(producto = pepperoni, cantidad = 1),
                ItemPedido(producto = jamonChampinon, cantidad = 1)
            ),
            fecha = fechaEnPromocion
        )

        assertEquals(23480.0, totales.subtotal, tolerancia)
        assertEquals(3522.0, totales.descuentoPromocion, tolerancia)
        assertEquals(19958.0, totales.total, tolerancia)
    }

    @Test
    fun limitesDelPeriodoEstanDentroDeLaPromocion() {
        assertTrue(estaEnPeriodoPromocion(LocalDate.of(2026, 11, 3)))
        assertTrue(estaEnPeriodoPromocion(LocalDate.of(2026, 11, 17)))
    }

    @Test
    fun diasFueraDelPeriodoNoEstanEnLaPromocion() {
        assertFalse(estaEnPeriodoPromocion(LocalDate.of(2026, 11, 2)))
        assertFalse(estaEnPeriodoPromocion(LocalDate.of(2026, 11, 18)))
    }
}
