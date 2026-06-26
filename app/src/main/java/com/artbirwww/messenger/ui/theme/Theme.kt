package com.artbirwww.messenger.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = White,
    onPrimary = Black,
    primaryContainer = DarkGray500,
    onPrimaryContainer = White,
    secondary = Gray400,
    onSecondary = Black,
    secondaryContainer = DarkGray600,
    onSecondaryContainer = White,
    background = DarkGray900,
    onBackground = White,
    surface = DarkGray800,
    onSurface = White,
    surfaceVariant = DarkGray700,
    onSurfaceVariant = Gray300,
    outline = DarkGray500
)

private val LightColorScheme = lightColorScheme(
    primary = Black,
    onPrimary = White,
    primaryContainer = Black,
    onPrimaryContainer = White,
    secondary = Gray500,
    onSecondary = White,
    secondaryContainer = Gray100,
    onSecondaryContainer = Black,
    background = White,
    onBackground = Black,
    surface = Gray50,
    onSurface = Black,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray500,
    outline = Gray200
)

@Composable
fun MessengerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
