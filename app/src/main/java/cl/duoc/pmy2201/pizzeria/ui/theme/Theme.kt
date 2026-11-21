package cl.duoc.pmy2201.pizzeria.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaAlbahacaYMasa: ColorScheme = lightColorScheme(
    primary = Albahaca,
    onPrimary = Color.White,
    primaryContainer = AlbahacaClaro,
    onPrimaryContainer = AlbahacaOscuro,
    secondary = Corteza,
    onSecondary = Color.White,
    secondaryContainer = CortezaClaro,
    onSecondaryContainer = Corteza,
    background = Masa,
    onBackground = Tinta,
    surface = Masa,
    onSurface = Tinta,
    surfaceVariant = Color.White,
    onSurfaceVariant = TextoSecundario,
    outline = Borde,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color.White,
    surfaceContainerHigh = MasaTostada,
    surfaceContainerHighest = MasaDorada,
    surfaceDim = MasaSombra,
    surfaceBright = Masa
)

/**
 * Tema de la app con la paleta "albahaca y masa".
 *
 * No usa color dinámico (Android 12+) ni esquema oscuro: la app es de uso interno y siempre debe verse
 * con los colores de la pizzería, de modo que estados como disponible/no disponible o la pizza seleccionada
 * se distingan igual en todos los equipos, sin depender del fondo de pantalla ni del modo del sistema.
 */
@Composable
fun PizzeriaAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaAlbahacaYMasa,
        typography = Typography,
        content = content
    )
}
