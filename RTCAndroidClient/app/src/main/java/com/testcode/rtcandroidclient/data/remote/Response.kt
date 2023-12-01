package com.testcode.rtcandroidclient.data.remote
data class Response(
    val type: ResponseType,
    val name: String,
    val data: String?
)
data class ListOfUserResData(
    val listOfUser: List<String>?
)
data class CallResponseData(
    val target: String
)
data class SdpResponseData(
    val sdp: String
)
data class IceResponseData(
    val sdpMLineIndex: Int,
    val sdpMid: String,
    val sdpCandidate: String
)
enum class ResponseType {
    LIST_OF_USER, CALL_REQUEST_RESPONSE, OFFER_RECEIVED_RESPONSE, ANSWER_RECEIVED_RESPONSE, ICE_CANDIDATE_RESPONSE
}