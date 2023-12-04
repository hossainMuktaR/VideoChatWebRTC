package com.testcode.rtcandroidclient.presentation.call_screen


import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun BottomCallController(
    items: MutableList<ControllerButton>,
    onEndClick: () -> Unit
) {
    NavigationBar {
        items.forEachIndexed { index, controllerButton ->
            NavigationBarItem(
                selected = index == 2,
                onClick = { if (index == 2) onEndClick() else controllerButton.onClick() },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Red
                ),
                icon = {
                    if (controllerButton.enabled) Icon(
                        controllerButton.enabledIcon,
                        contentDescription = null
                    ) else
                        Icon(controllerButton.icon, contentDescription = null)
                }
            )
        }
    }
}