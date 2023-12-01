package com.testcode.rtcandroidclient.presentation.user_list_screen


data class UserListState(
    val listOfMember: List<String> = emptyList(),
    val isCallRequest: Boolean = false,
    val callerName: String? = null,
    val userName: String? = null,
)
