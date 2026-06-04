package com.zerodev.clen.presentation.settings

import com.zerodev.clen.domain.model.ThemeMode

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColorEnabled: Boolean = true,
    val largeFileThresholdBytes: Long = 0L,
    val oldFileThresholdDays: Int = 0,
    val safGrantCount: Int = 0,
    val whitelistedCount: Int = 0,
    val hasAnyMediaAccess: Boolean = false,
    val hasFullMediaAccess: Boolean = false,
)
