package com.zerodev.clen

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.zerodev.clen.domain.model.SettingsModel
import com.zerodev.clen.domain.repository.SettingsRepository
import com.zerodev.clen.presentation.common.theme.ClenTheme
import com.zerodev.clen.presentation.common.theme.shouldUseDarkTheme
import com.zerodev.clen.presentation.navigation.ClenApp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settings by settingsRepository.settings.collectAsState(
                initial = SettingsModel(),
            )
            val darkTheme = shouldUseDarkTheme(settings.themeMode)

            SideEffect {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        lightScrim = Color.TRANSPARENT,
                        darkScrim = Color.TRANSPARENT,
                    ) { _ -> darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        lightScrim = Color.TRANSPARENT,
                        darkScrim = Color.TRANSPARENT,
                    ) { _ -> darkTheme },
                )
            }

            ClenTheme(settings = settings) {
                ClenApp(
                    onboardingCompleted = settings.onboardingCompleted,
                    onOnboardingCompleted = {
                        settingsRepository.setOnboardingCompleted(true)
                    },
                )
            }
        }
    }
}
