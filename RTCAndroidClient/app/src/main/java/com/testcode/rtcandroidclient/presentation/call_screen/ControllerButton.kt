package com.testcode.rtcandroidclient.presentation.call_screen

import androidx.compose.ui.graphics.vector.ImageVector

data class ControllerButton(
    val name: String,
    val enabled: Boolean,
    val icon: ImageVector,
    val enabledIcon: ImageVector,
    val onClick: () -> Unit
)
