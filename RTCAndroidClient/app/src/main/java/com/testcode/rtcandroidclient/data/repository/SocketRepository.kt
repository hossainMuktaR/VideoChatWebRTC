package com.testcode.rtcandroidclient.data.repository

import com.testcode.rtcandroidclient.data.remote.Response
import kotlinx.coroutines.flow.Flow
import org.webrtc.IceCandidate

interface SocketRepository {
    suspend fun initSocket(isEmulator: Boolean)

    suspend fun storeUser(userName: String)

    suspend fun getOnlineUser(userName: String)

    suspend fun receiveResponse(): Flow<Response>
    suspend fun sendOffer(userName: String,targetName: String, sdp: String)
    suspend fun sendCreateAnswer(userName: String,targetName: String, sdp: String)
    suspend fun sendIceCandidate(userName: String,target: String, iceCandidate: IceCandidate)
    suspend fun tryDisconnect()
}