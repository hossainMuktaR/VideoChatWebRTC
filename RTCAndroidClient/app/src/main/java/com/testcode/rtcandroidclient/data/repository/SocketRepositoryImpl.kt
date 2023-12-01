package com.testcode.rtcandroidclient.data.repository

import com.google.gson.Gson
import com.testcode.rtcandroidclient.data.remote.Response
import com.testcode.rtcandroidclient.data.remote.SocketClient
import com.testcode.rtcandroidclient.data.remote.dto.IceRequestData
import com.testcode.rtcandroidclient.data.remote.dto.Request
import com.testcode.rtcandroidclient.data.remote.dto.RequestType
import com.testcode.rtcandroidclient.data.remote.dto.SdpRequestData
import kotlinx.coroutines.flow.Flow
import org.webrtc.IceCandidate
import javax.inject.Inject

class SocketRepositoryImpl @Inject constructor(
    private val socketClient: SocketClient
) : SocketRepository {

    private val gson = Gson()
    override suspend fun initSocket(isEmulator: Boolean) {
        socketClient.initSocket(isEmulator)
    }

    override suspend fun storeUser(userName: String) {
        socketClient.sendRequest(
            Request(
                type = RequestType.STORE_USER,
                name = userName,
                data = null
            )
        )
    }

    override suspend fun getOnlineUser(userName: String) {
        socketClient.sendRequest(
            Request(
                type = RequestType.GET_ONLINE_USER,
                name = userName,
                data = null
            )
        )
    }

    override suspend fun receiveResponse(): Flow<Response> {
        return socketClient.receiveResponse()
    }

    override suspend fun sendOffer(userName: String, targetName: String, sdp: String) {
        socketClient.sendRequest(
            Request(
                type = RequestType.CREATE_OFFER,
                name = userName,
                data = gson.toJson(
                    SdpRequestData(
                        targetName,
                        sdp
                    )
                )
            )
        )
    }

    override suspend fun sendCreateAnswer(userName: String, targetName: String, sdp: String) {
        socketClient.sendRequest(
            Request(
                type = RequestType.CREATE_ANSWER,
                name = userName,
                data = gson.toJson(
                    SdpRequestData(
                        targetName,
                        sdp
                    )
                )
            )
        )
    }

    override suspend fun sendIceCandidate(
        userName: String,
        target: String,
        iceCandidate: IceCandidate
    ) {

        socketClient.sendRequest(
            Request(
                type = RequestType.ICE_CANDIDATE,
                name = userName,
                data = gson.toJson(
                    IceRequestData(
                        target = target,
                        sdpMLineIndex = iceCandidate.sdpMLineIndex,
                        sdpMid = iceCandidate.sdpMid,
                        sdpCandidate = iceCandidate.sdp
                    )
                )
            )
        )

    }

    override suspend fun tryDisconnect() {
        socketClient.tryDisconnect()
    }
}