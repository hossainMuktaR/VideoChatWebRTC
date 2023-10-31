package com.testcode.routes


import com.testcode.data.model.User
import com.testcode.domain.Request
import com.testcode.domain.type
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import java.util.Collections

fun Route.configSocketRoute() {
    val users = Collections.synchronizedSet<User?>(LinkedHashSet())
    webSocket("/") {
        try {
            for (frame in incoming) {
                if (frame !is Frame.Text) return@webSocket
                val request = Json.decodeFromString<Request>(frame.readText())
                val user = users.find { request.user?.name == it.name }
                when (request.type) {
                    type.STORE_USER -> {
                        if (user != null) {
                            //if user already exits
                            send(user.toString())
                            close()
                            return@webSocket
                        }
                        val newUser = request.user?.let {
                            User(
                                name = it.name,
                                conn = this
                            )
                        }
                        users.add(newUser)
                        send("User ${newUser?.name} Stored")
                    }

                    type.START_CALL -> {
                        val userToCall = users.find { request.data?.target == it.name }
                        if (userToCall != null) {
                            send("User is ready to call")
                        } else {
                            send("User not online")
                        }
                    }

                    type.CREATE_OFFER ->{
                        val userToReceiveCall = users.find { request.data?.target == it.name }
                        if (userToReceiveCall != null) {
                            userToReceiveCall.conn?.send("Ready to receive call from ${user?.name}")
                        }
                    }
                    type.CREATE_ANSWER -> {
                        val userToReceiveAnswer = users.find { request.data?.target == it.name }
                        if ( userToReceiveAnswer!= null) {
                            userToReceiveAnswer.conn?.send("Answer received call from ${user?.name}")
                        }
                    }
                    type.ICE_CANDIDATE -> {
                        val userToReceiveIceCandidate = users.find { request.data?.target == it.name }
                        if (userToReceiveIceCandidate != null) {
                            userToReceiveIceCandidate.conn?.send("received ice candidate")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println(e.localizedMessage)
        } finally {
            println("Removing thisConnection!")
//            connections -= thisConnection
        }
    }
}