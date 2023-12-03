package com.testcode.rtcandroidclient.data.remote

import android.util.Log
import com.google.gson.Gson
import com.testcode.rtcandroidclient.data.remote.dto.Request
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.url
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow

private val TAG = "singalingClient"

class SocketClient(
    private val client: HttpClient
) {
    private var socket: WebSocketSession? = null
    private val gson = Gson()
    suspend fun initSocket(isEmulator: Boolean) {
        try {
            socket = client.webSocketSession {
                url(if (isEmulator) "ws://10.0.2.2:3030/" else "ws://192.168.0.101:3030/")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("socket not connect")
        }
    }

    suspend fun sendRequest(request: Request) {
        try {
            val requestJson = gson.toJson(request)
            socket?.send(requestJson)
        } catch (e: Exception) {
            Log.d(TAG, "sendMessageToSocket: $e")
        }
    }

    suspend fun receiveResponse(): Flow<Response> {
        return try {
            socket?.incoming
                ?.receiveAsFlow()
                ?.filter {
                    it is Frame.Text
                }?.map {
                    val jsonString = (it as? Frame.Text)?.readText() ?: ""
                    println("response json: $jsonString")
                    val response = gson.fromJson(jsonString, Response::class.java)
                    println("response class: ${response}")
                    response
                } ?: flow {}
        } catch (e: Exception) {
            e.printStackTrace()
            flow { }
        }
    }

    suspend fun tryDisconnect() {
        socket?.close()
    }
}