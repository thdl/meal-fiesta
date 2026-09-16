package com.example.mealcase.core.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The app targets SDK 35+, where the system draws edge-to-edge by default — our content sits
 * behind the status and navigation bars unless a screen opts into insets itself. Screens with
 * a fixed header handle the status bar directly (see each screen's own inset padding); this
 * covers the one thing every scrollable screen needs: room at the bottom so the last row
 * isn't hidden under the gesture pill or 3-button nav bar.
 */
@Composable
fun navigationBarsBottomPadding(extra: Dp = 0.dp): Dp =
    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + extra
