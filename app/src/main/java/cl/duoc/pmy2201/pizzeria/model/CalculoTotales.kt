package cl.duoc.pmy2201.pizzeria.model

import java.time.LocalDate

const val PORCENTAJE_DESCUENTO: Double = 0.10
const val UMBRAL_PIZZAS_DESCUENTO: Int = 3
const val TASA_IVA: Double = 0.19

/** Porcentaje de la promoción especial, aplicado sobre el subtotal de los productos promocionables. */
const val PORCENTAJE_PROMOCION: Double = 0.15

/** Fechas (ambas incluidas) en que rige la promoción especial. */
val PERIODO_PROMOCION: ClosedRange<LocalDate> = LocalDate.of(2026, 11, 3)..LocalDate.of(2026, 11, 17)

/**
 * Totales de un pedido. Los precios incluyen IVA.
 *
 * @property subtotal Suma de los productos antes de los descuentos.
 * @property descuentoCantidad Monto descontado por llevar más de [UMBRAL_PIZZAS_DESCUENTO] pizzas.
 * @property descuentoPromocion Monto descontado por la promoción especial sobre los productos promocionables.
 * @property total Monto a pagar, con IVA incluido.
 * @property neto Total sin IVA.
 * @property iva IVA contenido en el total.
 */
data class TotalesPedido(
    val subtotal: Double,
    val descuentoCantidad: Double,
    val descuentoPromocion: Double,
    val total: Double,
    val neto: Double,
    val iva: Double
)

/**
 * Indica si [fecha] está dentro de [PERIODO_PROMOCION], incluyendo el primer y el último día.
 */
fun estaEnPeriodoPromocion(fecha: LocalDate): Boolean = fecha in PERIODO_PROMOCION

/**
 * Calcula los totales de un pedido y desglosa el IVA del total. Los dos descuentos se suman:
 * - [PORCENTAJE_DESCUENTO] sobre el subtotal completo, cuando la cantidad total de pizzas supera [UMBRAL_PIZZAS_DESCUENTO].
 * - [PORCENTAJE_PROMOCION] solo sobre el subtotal de los items promocionables (precio × cantidad, ver [esPromocionable]),
 *   cuando [fecha] está en [PERIODO_PROMOCION].
 *
 * @param items Productos del pedido, cada uno con su cantidad.
 * @param fecha Fecha del pedido; se recibe como parámetro para no depender del día actual.
 */
fun calcularTotales(items: List<ItemPedido>, fecha: LocalDate): TotalesPedido {
    val subtotal: Double = items.sumOf { item: ItemPedido -> item.subtotal() }
    val cantidadPizzas: Int = items.sumOf { item: ItemPedido -> item.cantidad }
    val subtotalPromocionable: Double = items
        .filter { item: ItemPedido -> esPromocionable(item.producto) }
        .sumOf { item: ItemPedido -> item.subtotal() }
    val descuentoCantidad: Double = if (cantidadPizzas > UMBRAL_PIZZAS_DESCUENTO) subtotal * PORCENTAJE_DESCUENTO else 0.0
    val descuentoPromocion: Double =
        if (estaEnPeriodoPromocion(fecha)) subtotalPromocionable * PORCENTAJE_PROMOCION else 0.0
    val total: Double = subtotal - descuentoCantidad - descuentoPromocion
    val neto: Double = total / (1 + TASA_IVA)
    val iva: Double = total - neto
    return TotalesPedido(
        subtotal = subtotal,
        descuentoCantidad = descuentoCantidad,
        descuentoPromocion = descuentoPromocion,
        total = total,
        neto = neto,
        iva = iva
    )
}
