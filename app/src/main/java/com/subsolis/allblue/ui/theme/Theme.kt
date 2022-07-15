package com.subsolis.compose

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.subsolis.ui.theme.AppTypography

private val LightThemeColors = lightColorScheme(

	primary = md_theme_light_primary,
	onPrimary = md_theme_light_onPrimary,
	primaryContainer = md_theme_light_primaryContainer,
	onPrimaryContainer = md_theme_light_onPrimaryContainer,
	secondary = md_theme_light_secondary,
	onSecondary = md_theme_light_onSecondary,
	secondaryContainer = md_theme_light_secondaryContainer,
	onSecondaryContainer = md_theme_light_onSecondaryContainer,
	tertiary = md_theme_light_tertiary,
	onTertiary = md_theme_light_onTertiary,
	tertiaryContainer = md_theme_light_tertiaryContainer,
	onTertiaryContainer = md_theme_light_onTertiaryContainer,
	error = md_theme_light_error,
	errorContainer = md_theme_light_errorContainer,
	onError = md_theme_light_onError,
	onErrorContainer = md_theme_light_onErrorContainer,
	background = md_theme_light_background,
	onBackground = md_theme_light_onBackground,
	surface = md_theme_light_surface,
	onSurface = md_theme_light_onSurface,
	surfaceVariant = md_theme_light_surfaceVariant,
	onSurfaceVariant = md_theme_light_onSurfaceVariant,
	outline = md_theme_light_outline,
	inverseOnSurface = md_theme_light_inverseOnSurface,
	inverseSurface = md_theme_light_inverseSurface,
	inversePrimary = md_theme_light_inversePrimary,
//	shadow = md_theme_light_shadow,
)

private val DarkThemeColors = darkColorScheme(

	primary = md_theme_dark_primary,
	onPrimary = md_theme_dark_onPrimary,
	primaryContainer = md_theme_dark_primaryContainer,
	onPrimaryContainer = md_theme_dark_onPrimaryContainer,
	secondary = md_theme_dark_secondary,
	onSecondary = md_theme_dark_onSecondary,
	secondaryContainer = md_theme_dark_secondaryContainer,
	onSecondaryContainer = md_theme_dark_onSecondaryContainer,
	tertiary = md_theme_dark_tertiary,
	onTertiary = md_theme_dark_onTertiary,
	tertiaryContainer = md_theme_dark_tertiaryContainer,
	onTertiaryContainer = md_theme_dark_onTertiaryContainer,
	error = md_theme_dark_error,
	errorContainer = md_theme_dark_errorContainer,
	onError = md_theme_dark_onError,
	onErrorContainer = md_theme_dark_onErrorContainer,
	background = md_theme_dark_background,
	onBackground = md_theme_dark_onBackground,
	surface = md_theme_dark_surface,
	onSurface = md_theme_dark_onSurface,
	surfaceVariant = md_theme_dark_surfaceVariant,
	onSurfaceVariant = md_theme_dark_onSurfaceVariant,
	outline = md_theme_dark_outline,
	inverseOnSurface = md_theme_dark_inverseOnSurface,
	inverseSurface = md_theme_dark_inverseSurface,
	inversePrimary = md_theme_dark_inversePrimary,
//	shadow = md_theme_dark_shadow,
)

//@Composable
//fun AppTheme(
//useDarkTheme: Boolean = isSystemInDarkTheme(),
//content: @Composable() () -> Unit
//) {
//val colors = if (!useDarkTheme) {
//  LightThemeColors
//} else {
//  DarkThemeColors
//}
//

@Composable
fun Material3AppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
	val android12Higher = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

	val colorScheme = when {
		android12Higher && darkTheme -> {
			dynamicDarkColorScheme(LocalContext.current)

			// Update all of the system bar colors to be transparent, and use
			// dark icons if we're in light theme
//			systemUiController.setSystemBarsColor(
//				color = Transparent,
//				darkIcons = useDarkTheme
//			)

			// setStatusBarsColor() and setNavigationBarColor() also exist

		}
		android12Higher && !darkTheme -> {
			dynamicLightColorScheme(LocalContext.current)

			// Update all of the system bar colors to be transparent, and use
//			// dark icons if we're in light theme
//			systemUiController.setSystemBarsColor(
//				color = Transparent,
//			)
		}
		darkTheme -> {
			DarkThemeColors

//			systemUiController.setSystemBarsColor(
//				color = Transparent,
//				darkIcons = useDarkTheme
//			)
		}
		else -> {
			LightThemeColors
//			systemUiController.setSystemBarsColor(
//				color = Transparent,
//			)
		}
	}

	val systemUiController = rememberSystemUiController()
	val useDarkTheme: Boolean = isSystemInDarkTheme()

	SideEffect {
		// Update all of the system bar colors to be transparent, and use
		// dark icons if we're in light theme
		if (!useDarkTheme) {
			systemUiController.setSystemBarsColor(
				color = md_theme_light_surface,
				darkIcons = !useDarkTheme
			)
		} else {
			systemUiController.setSystemBarsColor(
				color = md_theme_dark_surface,
			)
		}

		// setStatusBarsColor() and setNavigationBarColor() also exist
	}


	MaterialTheme(
		colorScheme = colorScheme,
		typography = AppTypography,
		content = content,
	)

}