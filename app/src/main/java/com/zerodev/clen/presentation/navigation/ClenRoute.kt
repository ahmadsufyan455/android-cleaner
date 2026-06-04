package com.zerodev.clen.presentation.navigation

sealed class ClenRoute(val route: String) {
    data object Onboarding : ClenRoute("onboarding")
    data object Home : ClenRoute("home")
    data object Scan : ClenRoute("scan")
    data object ScanResults : ClenRoute("scan_results")
    data object Trash : ClenRoute("trash")
    data object Settings : ClenRoute("settings")
}
