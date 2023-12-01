package com.testcode.rtcandroidclient.presentation.user_list_screen

import android.app.Application
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.testcode.rtcandroidclient.data.rtc.PeerConnectionObserver
import com.testcode.rtcandroidclient.data.rtc.RtcClient
import com.testcode.rtcandroidclient.common.Constant
import com.testcode.rtcandroidclient.data.remote.IceResponseData
import com.testcode.rtcandroidclient.data.remote.ListOfUserResData
import com.testcode.rtcandroidclient.data.remote.ResponseType
import com.testcode.rtcandroidclient.data.remote.SdpResponseData
import com.testcode.rtcandroidclient.data.repository.SocketRepository
import com.testcode.rtcandroidclient.presentation.login_screen.LoginSideEffect
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.RtpReceiver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import javax.inject.Inject

private val TAG = "userListViewModel"

@HiltViewModel
class UserlistViewModel @Inject constructor(
    application: Application,
    private val socketRepository: SocketRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private lateinit var localRender: SurfaceViewRenderer
    private lateinit var remoteRender: SurfaceViewRenderer

    private val gson = Gson()
    lateinit var rtcClient: RtcClient

    private val _state = mutableStateOf(UserListState())
    val state: State<UserListState> = _state

//    private val _sideEffect = MutableSharedFlow<UserListSideEffect>()
//    val sideEffect: SharedFlow<UserListSideEffect> = _sideEffect


    init {
        _state.value = state.value.copy(
            userName = savedStateHandle.get<String>(Constant.USERNAMEKEY) ?: "Guest"
        )
        Log.e(TAG, "Your UserName: ${state.value.userName}")
        viewModelScope.launch {
            observeResponse()
            requestGetOnlineUser(state.value.userName!!)

        }
        rtcClient = RtcClient(application, socketRepository, object : PeerConnectionObserver() {
            override fun onIceCandidate(iceCandidate: IceCandidate?) {
                super.onIceCandidate(iceCandidate)
                rtcClient.addIceCandidate(iceCandidate)
                Log.d(TAG, "onIcecanditate called from callviewmodel")
                viewModelScope.launch {
                    socketRepository.sendIceCandidate(
                        state.value.userName!!,
                        state.value.callerName!!,
                        iceCandidate!!
                    )
                }
            }

            override fun onAddStream(mediaStream: MediaStream?) {
                super.onAddStream(mediaStream)
                mediaStream?.videoTracks?.get(0)?.addSink(remoteRender)
                Log.d(TAG, "onAddStream call")
            }

//            override fun onAddTrack(
//                rtpReceiver: RtpReceiver?,
//                mediaStreams: Array<out MediaStream>?
//            ) {
//                super.onAddTrack(rtpReceiver, mediaStreams)
//                mediaStreams?.get(0)?.videoTracks?.get(0)?.addSink(remoteRender)
//                Log.d(TAG, "onAddTrack call")
//            }

            override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState?) {
                Log.d(TAG, "ICE Connection State: $iceConnectionState")
            }
        }).apply {
            setScope(viewModelScope)
        }
    }

    fun requestGetOnlineUser(userName: String) {
        viewModelScope.launch {
            socketRepository.getOnlineUser(userName)
            println("online user request send")
        }
    }

    fun setLocalRenderView(localRenderer: SurfaceViewRenderer) {
        localRender = localRenderer
        rtcClient.initSurfaceView(localRender)
        rtcClient.startLocalVideo(localRender)
        Log.e(TAG, "start video capture start")
    }

    fun setRemoteRenderView(remoteRenderer: SurfaceViewRenderer) {
        remoteRender = remoteRenderer
        rtcClient.initSurfaceView(remoteRender)
    }

    private suspend fun observeResponse() {
        socketRepository.receiveResponse().onEach { response ->
            when (response.type) {
                ResponseType.LIST_OF_USER -> {
                    val listOfuserData = gson.fromJson(response.data, ListOfUserResData::class.java)
                    _state.value = state.value.copy(
                        listOfMember = listOfuserData.listOfUser ?: emptyList()
                    )
                    Log.e(TAG, "list of user response: ${listOfuserData}")
                }

                ResponseType.CALL_RESPONSE -> {}
                ResponseType.OFFER_RECEIVED_RESPONSE -> {
                    val sdpResponseData = gson.fromJson(response.data, SdpResponseData::class.java)
                    val session = SessionDescription(
                        SessionDescription.Type.OFFER,
                        sdpResponseData.sdp
                    )
                    _state.value = state.value.copy(
                        callerName = response.name,
                        isCallStart = true
                    )
                    Log.e(TAG, "sdp response from offer: ${sdpResponseData.sdp}")
                    rtcClient.onRemoteSessionReceived(session)
                    rtcClient.answer(state.value.userName!!, response.name)

                    //2nd user call screen start
                }

                ResponseType.ANSWER_RECEIVED_RESPONSE -> {
                    val sdpResponseData = gson.fromJson(response.data, SdpResponseData::class.java)
                    val session = SessionDescription(
                        SessionDescription.Type.ANSWER,
                        sdpResponseData.sdp
                    )
                    Log.e(TAG, "sdp response from answer: ${sdpResponseData.sdp}")
                    rtcClient.onRemoteSessionReceived(session)
//                    viewModelScope.launch {
//                        _sideEffect.emit(UserListSideEffect.GoCallScreen(state.value.userName!!, state.value.callerName!!))
//                    }
                    //1st user call screen start
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
                        Log.e(TAG, "ice response: ${iceResponsedata.sdpCandidate}")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

        }.launchIn(viewModelScope)
    }

    fun call(target: String) {
        _state.value = state.value.copy(
            callerName = target,
            isIncomingCall = false,
            isCallStart = true
        )
        rtcClient.call(state.value.userName!!, target)
    }

    fun answer(target: String) {
        rtcClient.answer(state.value.userName!!, target)
//        viewModelScope.launch {
//            _sideEffect.emit(UserListSideEffect.GoCallScreen(state.value.userName!!, target))
//        }
        _state.value = state.value.copy(
            isIncomingCall = false,
            isCallStart = true
        )

    }

    fun callDismiss() {
        _state.value = state.value.copy(
            isIncomingCall = false,
            callerName = null,
            isCallStart = false,
        )
        closeSocket()
    }

    fun closeSocket() {
        viewModelScope.launch {
            socketRepository.tryDisconnect()
        }
    }

    override fun onCleared() {
        super.onCleared()
        closeSocket()
    }

}

sealed class UserListSideEffect {
    class GoCallScreen(val userName: String, val targetName: String) : UserListSideEffect()
}

