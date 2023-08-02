package com.jobik.shkiper.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.*
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorPalette = darkColors(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    error = md_theme_light_error,
    onError = md_theme_light_onError,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
)

private val LightColorPalette = lightColors(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    error = md_theme_dark_error,
    onError = md_theme_dark_onError,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
)

object CustomAppTheme {
    val colors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
//    val typography: Typography
//        @Composable
//        get() = MaterialTheme.typography
//
//    val shapes: Shapes
//        @Composable
//        get() = MaterialTheme.shapes
}

@Composable
fun CustomAppTheme(
    themeColor: ExtendedColors,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalExtendedColors provides themeColor) {
        val systemUiController = rememberSystemUiController()
        systemUiController.setSystemBarsColor(
            color = themeColor.mainBackground
        )
        MaterialTheme(
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
