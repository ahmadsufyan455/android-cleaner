package com.zerodev.clen.presentation.common.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.zerodev.clen.domain.model.SettingsModel
import com.zerodev.clen.domain.model.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = Teal80,
    onPrimary = OnTeal80,
    secondary = Amber80,
    onSecondary = OnAmber80,
    tertiary = Blue80,
    onTertiary = OnBlue80,
    background = Neutral10,
    onBackground = Neutral90,
    surface = Neutral10,
    onSurface = Neutral90,
    surfaceVariant = Neutral20,
    onSurfaceVariant = Neutral90,
)

private val LightColorScheme = lightColorScheme(
    primary = Teal40,
    onPrimary = OnTeal40,
    secondary = Amber40,
    onSecondary = OnAmber40,
    tertiary = Blue40,
    onTertiary = OnBlue40,
    background = Neutral98,
    onBackground = Neutral10,
    surface = Neutral98,
    onSurface = Neutral10,
    surfaceVariant = Neutral95,
    onSurfaceVariant = Neutral20,
)

@Composable
fun ClenTheme(
    settings: SettingsModel,
    content: @Composable () -> Unit,
) {
    ClenTheme(
        darkTheme = shouldUseDarkTheme(settings.themeMode),
        dynamicColor = settings.dynamicColorEnabled,
        content = content,
    )
}

@Composable
fun ClenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}

@Composable
fun shouldUseDarkTheme(themeMode: ThemeMode): Boolean = when (themeMode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}
