package com.testcode.routes


import com.google.gson.Gson
import com.testcode.data.model.User
import com.testcode.domain.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import java.util.*


fun Route.configSocketRoute() {
    val users = Collections.synchronizedSet<User>(LinkedHashSet())
    var user: User? = null
    val gson = Gson()

    webSocket("/") {
        try {
            for (frame in incoming) {
                if (frame !is Frame.Text) return@webSocket
                val requestData = gson.fromJson(frame.readText(), Request::class.java)
                println("Request from ${requestData.name}: $requestData")
                user = users.find { requestData.name == it.name }
                when (requestData.type) {
                    RequestType.STORE_USER -> {
                        if (user != null) {
                            close()
                            return@webSocket
                        }
                        user = User(
                            name = requestData.name ?: "Guest",
                            conn = this
                        )
                        users.add(user!!)
                        println("user added. name: ${user?.name}")

                    }

                    RequestType.GET_ONLINE_USER -> {
                        val listOfOnlineUser = users.filter { user?.name != it.name }
                        val listOfUserData = gson.toJson(ListOfUserResData(
                            listOfUser = listOfOnlineUser.map { it.name }
                        ))
                        val response = gson.toJson(
                            Response(
                                type = ResponseType.LIST_OF_USER,
                                name = user?.name ?: "Guest",
                                data = listOfUserData
                            )
                        )
                        send(response)
                        println("list of user response: $response")
                    }

                    RequestType.CALL_REQUEST -> {
                        val data = gson.fromJson(requestData.data, CallRequestData::class.java)
                        val userToCall = users.find { data.target == it.name }
                        if (userToCall != null) {
                            val response = gson.toJson(
                                Response(
                                    ResponseType.CALL_REQUEST_RESPONSE,
                                    name = data.target,
                                    data = gson.toJson(CallResponseData(target = requestData.name))
                                )
                            )
                            println("Call request come from: ${requestData.name}")
                            println("Call response send to: ${data.target}")
                            userToCall.conn?.send(response)
                        } else {
                            val response = gson.toJson(
                                Response(
                                    ResponseType.CALL_REQUEST_RESPONSE,
                                    name = requestData.name,
                                    data = gson.toJson(CallResponseData(target = null))
                                )
                            )
                            user?.conn?.send(response)
                        }
                    }
                    RequestType.ANSWER_REQUEST -> {
                        val data = gson.fromJson(requestData.data, CallRequestData::class.java)
                        val userToCall = users.find { data.target == it.name }
                        if (userToCall != null) {
                            val response = gson.toJson(
                                Response(
                                    ResponseType.ANSWER_REQUEST_RESPONSE,
                                    name = data.target,
                                    data = gson.toJson(CallResponseData(target = requestData.name))
                                )
                            )
                            delay(2000)
                            println("answer request come from: ${requestData.name}")
                            println("answer response send to: ${data.target}")
                            userToCall.conn?.send(response)
                        } else {
                            val response = gson.toJson(
                                Response(
                                    ResponseType.CALL_REQUEST_RESPONSE,
                                    name = requestData.name,
                                    data = gson.toJson(CallResponseData(target = null))
                                )
                            )
                            user?.conn?.send(response)
                        }
                    }

                    RequestType.CREATE_OFFER -> {
                        val data = gson.fromJson(requestData.data, SdpRequestData::class.java)
                        val userToReceiveCall = users.find { data.target == it.name }
                        if (userToReceiveCall != null) {
                            val sdpData = gson.toJson(
                                SdpResponseData(
                                    sdp = data.sdp
                                )
                            )
                            val response = gson.toJson(
                                Response(
                                    ResponseType.OFFER_RECEIVED_RESPONSE,
                                    name = requestData.name,
                                    data = sdpData
                                )
                            )
                            println("sdp offer String: ${data.sdp}")
                            println("sdp response: ${response}")
                            userToReceiveCall.conn?.send(response)
                        }
                    }

                    RequestType.CREATE_ANSWER -> {
                        val data = gson.fromJson(requestData.data, SdpRequestData::class.java)
                        val userToReceiveAnswer = users.find { data.target == it.name }
                        if (userToReceiveAnswer != null) {
                            val response = gson.toJson(
                                Response(
                                    ResponseType.ANSWER_RECEIVED_RESPONSE,
                                    name = requestData.name,
                                    data = gson.toJson(
                                        SdpResponseData(
                                            sdp = data.sdp
                                        )
                                    )
                                )
                            )
                            println("sdp answer String: ${data.sdp}")
                            userToReceiveAnswer.conn?.send(response)
                        }
                    }

                    RequestType.ICE_CANDIDATE -> {
                        val data = gson.fromJson(requestData.data, IceRequestData::class.java)
                        val userToReceiveIceCandidate = users.find { data.target == it.name }
                        if (userToReceiveIceCandidate != null) {
                            val response = gson.toJson(
                                Response(
                                    ResponseType.ICE_CANDIDATE_RESPONSE,
                                    name = requestData.name,
                                    data = gson.toJson(
                                        IceResponseData(
                                            sdpMLineIndex = data.sdpMLineIndex,
                                            sdpMid = data.sdpMid,
                                            sdpCandidate = data.sdpCandidate,
                                        )
                                    )
                                )
                            )
                            userToReceiveIceCandidate.conn?.send(response)
                        }
                    }
                }
            }
        } catch (e: ClosedReceiveChannelException) {
            println("ClosedReceiveChannelException Found")
            user.let {
                users.remove(it)
            }
            user = null
        } catch (e: Exception) {
            println(e.localizedMessage)
        } finally {
            println("Removing thisConnection!")
            user.let {
                users.remove(it)
            }
            user = null
        }
    }
}