package com.testcode.data.model

import io.ktor.websocket.*

data class User(
    val name: String,
    val conn: DefaultWebSocketSession?
)