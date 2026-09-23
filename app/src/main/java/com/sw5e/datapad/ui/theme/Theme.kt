package com.sw5e.datapad.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val SpaceBlack = Color(0xFF0B0E14)
val DarkSlate = Color(0xFF161B22)
val HoloBlue = Color(0xFF00D2FF)
val NeonAmber = Color(0xFFFFB000)
val SithRed = Color(0xFFFF3B30)
val OffWhite = Color(0xFFE6EDF3)
val CardBorder = Color(0xFF30363D)

private val DarkColorScheme = darkColorScheme(
    primary = HoloBlue,
    secondary = NeonAmber,
    tertiary = SithRed,
    background = SpaceBlack,
    surface = DarkSlate,
    onPrimary = SpaceBlack,
    onBackground = OffWhite,
    onSurface = OffWhite
)

@Composable
fun SW5ETheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColorScheme, content = content)
}