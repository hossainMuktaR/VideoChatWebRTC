package com.testcode.rtcandroidclient.data.remote.dto

data class Request(
    val type: RequestType,
    val name: String,
    val data: String?
)
data class CallRequestData(
    val target: String
)
data class SdpRequestData(
    val target: String,
    val sdp: String,
)
data class IceRequestData(
    val target: String,
    val sdpMLineIndex: Int,
    val sdpMid: String,
    val sdpCandidate: String
)
enum class RequestType {
    STORE_USER, GET_ONLINE_USER, CALL_REQUEST, ANSWER_REQUEST, CREATE_OFFER, CREATE_ANSWER, ICE_CANDIDATE
}