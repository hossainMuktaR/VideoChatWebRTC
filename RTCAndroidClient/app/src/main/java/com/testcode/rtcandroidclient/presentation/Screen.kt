package com.testcode.rtcandroidclient.presentation

sealed class Screen(val route: String) {
    object LoginScreen: Screen("login_screen")
    object UserListScreen: Screen("user_list_screen")
    object CallScreen: Screen("call_screen")
}
