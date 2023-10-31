package com.testcode.plugins

import com.testcode.routes.configSocketRoute
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*

fun Application.configureRouting() {
    routing {
        configSocketRoute()
    }
}
