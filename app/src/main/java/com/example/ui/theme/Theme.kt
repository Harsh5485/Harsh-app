package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val NuvioColorScheme = darkColorScheme(
    primary = NuvioPrimary,
    onPrimary = NuvioBackground,
    primaryContainer = NuvioSurfaceVariant,
    onPrimaryContainer = NuvioPrimaryBright,
    secondary = NuvioSecondary,
    onSecondary = NuvioBackground,
    secondaryContainer = NuvioSurfaceVariant,
    onSecondaryContainer = NuvioSecondary,
    tertiary = NuvioTertiary,
    background = NuvioBackground,
    onBackground = NuvioTextPrimary,
    surface = NuvioSurface,
    onSurface = NuvioTextPrimary,
    surfaceVariant = NuvioSurfaceVariant,
    onSurfaceVariant = NuvioTextSecondary,
    outline = NuvioCardBorder,
    outlineVariant = NuvioCardBorder.copy(alpha = 0.5f)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val windowInsetsController = WindowCompat.getInsetsController(window, view)
                windowInsetsController.isAppearanceLightStatusBars = false
                windowInsetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = NuvioColorScheme,
        typography = Typography,
        content = content
    )
}
