package com.testcode.rtcandroidclient.presentation.call_screen

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.testcode.rtcandroidclient.common.Constant
import com.testcode.rtcandroidclient.data.remote.CallResponseData
import com.testcode.rtcandroidclient.data.remote.IceResponseData
import com.testcode.rtcandroidclient.data.remote.ListOfUserResData
import com.testcode.rtcandroidclient.data.remote.ResponseType
import com.testcode.rtcandroidclient.data.remote.SdpResponseData
import com.testcode.rtcandroidclient.data.repository.SocketRepository
import com.testcode.rtcandroidclient.data.rtc.PeerConnectionObserver
import com.testcode.rtcandroidclient.data.rtc.RtcClient
import com.testcode.rtcandroidclient.presentation.user_list_screen.UserListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import javax.inject.Inject

private val TAG = "CallViewModel"

@HiltViewModel
class CallViewModel @Inject constructor(
    private val rtcClient: RtcClient,
    private val socketRepository: SocketRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private lateinit var localRender: SurfaceViewRenderer
    private lateinit var remoteRender: SurfaceViewRenderer

    private var userName: String
    private var targetUserName: String
    private val gson = Gson()

    private val _state = mutableStateOf(CallScreenState())
    val state: State<CallScreenState> = _state


    init {
        userName = savedStateHandle.get<String>(Constant.USERNAMEKEY) ?: "Guest"
        targetUserName = savedStateHandle.get<String>(Constant.TARGETNAMEKEY) ?: ""
        _state.value = state.value.copy(
            userName = userName,
            callerName = targetUserName
        )
        viewModelScope.launch {
            observeResponse()
        }
        rtcClient.run {
            setScope(viewModelScope)
            setObserver(object : PeerConnectionObserver() {
                override fun onIceCandidate(iceCandidate: IceCandidate?) {
                    super.onIceCandidate(iceCandidate)
                    rtcClient.addIceCandidate(iceCandidate)
                    Log.d(TAG, "onIcecanditate called from callviewmodel")
                    viewModelScope.launch {
                        socketRepository.sendIceCandidate(userName, targetUserName, iceCandidate!!)
                    }
                }

                override fun onAddStream(mediaStream: MediaStream?) {
                    super.onAddStream(mediaStream)
                    mediaStream?.videoTracks?.get(0)?.addSink(remoteRender)
                    Log.d(TAG, "onAddStream call")
                }
            })
        }

    }

    fun setLocalRenderView(localRenderer: SurfaceViewRenderer) {
        localRender = localRenderer
        rtcClient.initSurfaceView(localRender)
        rtcClient.startLocalVideo(localRender)
        Log.e(
            TAG,
            "start video capture start"
        )
    }

    fun setRemoteRenderView(remoteRenderer: SurfaceViewRenderer) {
        remoteRender = remoteRenderer
        rtcClient.initSurfaceView(remoteRender)
    }

    private suspend fun observeResponse() {
        socketRepository.receiveResponse().onEach { response ->
            when (response.type) {
                ResponseType.OFFER_RECEIVED_RESPONSE -> {
                    val sdpResponseData = gson.fromJson(response.data, SdpResponseData::class.java)
                    val session = SessionDescription(
                        SessionDescription.Type.OFFER,
                        sdpResponseData.sdp
                    )

                    Log.e(
                        TAG,
                        "sdp response from offer: ${sdpResponseData.sdp}"
                    )
                    rtcClient.onRemoteSessionReceived(session)
                    rtcClient.answer(state.value.userName!!, state.value.callerName!!)
                }

                ResponseType.ANSWER_RECEIVED_RESPONSE -> {
                    val sdpResponseData = gson.fromJson(response.data, SdpResponseData::class.java)
                    val session = SessionDescription(
                        SessionDescription.Type.ANSWER,
                        sdpResponseData.sdp
                    )
                    Log.e(
                        TAG,
                        "sdp response from answer: ${sdpResponseData.sdp}"
                    )
                    rtcClient.onRemoteSessionReceived(session)
                }

                ResponseType.ICE_CANDIDATE_RESPONSE -> {
                    val iceResponsedata = gson.fromJson(response.data, IceResponseData::class.java)
                    try {
                        rtcClient.addIceCandidate(
                            IceCandidate(
                                iceResponsedata.sdpMid,
                                iceResponsedata.sdpMLineIndex,
                                iceResponsedata.sdpCandidate
                            )
                        )
                        Log.e(
                            TAG,
                            "ice response: ${iceResponsedata.sdpCandidate}"
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                else -> {}
            }

        }.launchIn(viewModelScope)
    }
}