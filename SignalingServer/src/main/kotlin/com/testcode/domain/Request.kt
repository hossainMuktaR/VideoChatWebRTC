package com.testcode.domain

import com.testcode.data.model.User
import kotlinx.serialization.Serializable

@Serializable
data class Request(
    val type: type,
    val user: User?,
    val data: RequestData?
)

@Serializable
data class RequestData(
    val target: String,
    val sdp: String?,
    val ice: Ice?
)
@Serializable
data class Ice (
    val sdpMLineIndex: String,
    val sdpMid: String,
    val sdpCandidate: String
)

enum class type {
    STORE_USER, START_CALL, CREATE_OFFER, CREATE_ANSWER, ICE_CANDIDATE
}