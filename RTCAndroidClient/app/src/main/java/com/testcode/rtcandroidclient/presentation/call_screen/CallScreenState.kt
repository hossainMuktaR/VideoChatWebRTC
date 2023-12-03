package com.testcode.rtcandroidclient.presentation.call_screen


data class CallScreenState(
    val callerName: String? = null,
    val userName: String? = null,
    val isMute: Boolean = false,
    val isVidePaused: Boolean = false,
    val isSpeakerMode: Boolean = true
)
