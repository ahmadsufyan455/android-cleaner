package com.zerodev.clen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.zerodev.clen.presentation.common.theme.ClenTheme
import com.zerodev.clen.presentation.navigation.ClenApp
import com.zerodev.clen.domain.model.SettingsModel
import com.zerodev.clen.domain.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by settingsRepository.settings.collectAsState(
                initial = SettingsModel(),
            )

            ClenTheme {
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
