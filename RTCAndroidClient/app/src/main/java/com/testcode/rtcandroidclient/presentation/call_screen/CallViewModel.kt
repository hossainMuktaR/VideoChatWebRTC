package com.testcode.rtcandroidclient.presentation.call_screen

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.testcode.rtcandroidclient.common.Constant
import com.testcode.rtcandroidclient.data.remote.CallResponseData
import com.testcode.rtcandroidclient.data.remote.IceResponseData
import com.testcode.rtcandroidclient.data.remote.ResponseType
import com.testcode.rtcandroidclient.data.remote.SdpResponseData
import com.testcode.rtcandroidclient.data.repository.SocketRepository
import com.testcode.rtcandroidclient.data.rtc.PeerConnectionObserver
import com.testcode.rtcandroidclient.data.rtc.RtcAudioManager
import com.testcode.rtcandroidclient.data.rtc.RtcClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
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
    private val rtcAudioManager: RtcAudioManager,
    private val socketRepository: SocketRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var _state by mutableStateOf(CallScreenState())
    val state: CallScreenState = _state

    private lateinit var localRender: SurfaceViewRenderer
    private lateinit var remoteRender: SurfaceViewRenderer

    private var userName: String
    private var targetUserName: String
    private val gson = Gson()
    val cBtnList = mutableListOf<ControllerButton>(
        ControllerButton(
            name = "Mic",
            enabled = _state.isMute,
            icon = Icons.Default.Mic,
            enabledIcon = Icons.Default.MicOff,
            onClick = {
                micToggle(_state.isMute)
            }
        ),
        ControllerButton(
            name = "Video Off",
            enabled = _state.isVidePaused,
            icon = Icons.Default.Videocam,
            enabledIcon = Icons.Default.VideocamOff,
            onClick = {
                cameraToggle(_state.isVidePaused)
            }
        ),
        ControllerButton(
            name = "Call End",
            enabled = true,
            icon = Icons.Default.CallEnd,
            enabledIcon = Icons.Default.CallEnd,
            onClick = {}
        ),
        ControllerButton(
            name = "Camera Switch",
            enabled = false,
            icon = Icons.Default.Cameraswitch,
            enabledIcon = Icons.Default.Cameraswitch,
            onClick = {
                cameraSwitch()
            }
        ),
        ControllerButton(
            name = "Audio Output",
            enabled = _state.isSpeakerMode,
            icon = Icons.Default.HeadsetMic,
            enabledIcon = Icons.Default.Speaker,
            onClick = {
                changeAudioOuput(_state.isSpeakerMode)
            }
        ),

    )

    init {
        userName = savedStateHandle.get<String>(Constant.USERNAMEKEY) ?: "Guest"
        targetUserName = savedStateHandle.get<String>(Constant.TARGETNAMEKEY) ?: ""
        _state = state.copy(
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
        rtcAudioManager.selectAudioDevice(RtcAudioManager.AudioDevice.SPEAKER_PHONE)
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
            Log.d(TAG, "Received response in call viemodel: $response")
            when (response.type) {
                ResponseType.ANSWER_REQUEST_RESPONSE -> {
                    val callResponseData =
                        gson.fromJson(response.data, CallResponseData::class.java)
                    callResponseData.target?.let {
                        _state = _state.copy(
                            callerName = it,
                        )
                        sendOfferRequest(response.name, it)
                    }
                    Log.e(
                        TAG,
                        "answer request response"
                    )
                }

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
                    rtcClient.answer(_state.userName!!, _state.callerName!!)
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

        }
            .catch { e ->
                Log.e(TAG, "Error in observeResponse in callviewmodel: ${e.message}")
            }.launchIn(viewModelScope)
    }

    private fun sendOfferRequest(userName: String, target: String) {
        rtcClient.startCall(userName, target)
    }

    fun callEnd() {
        rtcClient.endCall()
        viewModelScope.launch {
            socketRepository.tryDisconnect()
        }
    }
    fun micToggle(state: Boolean) {
        if(state){
            _state = _state.copy(
                isMute = false
            )
        }else{
            _state = _state.copy(
                isMute = true
            )
        }
        rtcClient.enableAudio(_state.isMute)
    }
    fun cameraToggle(state: Boolean) {
        if(state){
            _state = _state.copy(
                isVidePaused = false
            )
        }else{
            _state = _state.copy(
                isVidePaused = true
            )
        }
        rtcClient.enableVideo(_state.isVidePaused)
    }
    fun cameraSwitch() {
        rtcClient.switchCamera()
    }
    fun changeAudioOuput(state: Boolean) {
        if(state) {
            _state = _state.copy(
                isSpeakerMode = false
            )
            rtcAudioManager.setDefaultAudioDevice(
                RtcAudioManager.AudioDevice.EARPIECE
            )
        } else {
            _state = _state.copy(
                isSpeakerMode = true
            )
            rtcAudioManager.setDefaultAudioDevice(
                RtcAudioManager.AudioDevice.SPEAKER_PHONE
            )
        }
    }
}