package com.testcode.data.model

import io.ktor.websocket.*
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val name: String,
    val conn: DefaultWebSocketSession?
)