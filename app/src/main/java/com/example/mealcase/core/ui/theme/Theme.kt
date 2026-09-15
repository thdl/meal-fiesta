package com.example.mealcase.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Terracotta = Color(0xFFB4462A)
private val TerracottaLight = Color(0xFFFFB4A0)

private val LightColors = lightColorScheme(
    primary = Terracotta,
    secondary = Color(0xFF77574E),
)

private val DarkColors = darkColorScheme(
    primary = TerracottaLight,
    secondary = Color(0xFFE7BDB2),
)

@Composable
fun MealCaseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
