package com.testcode.rtcandroidclient.presentation.call_screen

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.testcode.rtcandroidclient.common.Constant
import com.testcode.rtcandroidclient.data.remote.IceResponseData
import com.testcode.rtcandroidclient.data.remote.ResponseType
import com.testcode.rtcandroidclient.data.repository.SocketRepository
import com.testcode.rtcandroidclient.data.rtc.PeerConnectionObserver
import com.testcode.rtcandroidclient.data.rtc.RtcClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.SurfaceViewRenderer
import javax.inject.Inject

private val TAG = "CallViewModel"

@HiltViewModel
class CallViewModel @Inject constructor(
//    private val rtcClient: RtcClient,
    private val socketRepository: SocketRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private lateinit var localRender: SurfaceViewRenderer
    private lateinit var remoteRender: SurfaceViewRenderer
    private var userName: String
    private var targetUserName: String
    private val gson = Gson()


    init {
        userName = savedStateHandle.get<String>(Constant.USERNAMEKEY) ?: "Guest"
        targetUserName = savedStateHandle.get<String>(Constant.TARGETNAMEKEY) ?: ""
//        viewModelScope.launch {
//            observeResponse()
//        }
//        rtcClient.run {
//            setScope(viewModelScope)
//            setPeerConnection(object : PeerConnectionObserver() {
//                override fun onIceCandidate(iceCandidate: IceCandidate?) {
//                    super.onIceCandidate(iceCandidate)
//                    rtcClient.addIceCandidate(iceCandidate)
//                    Log.d(TAG,"onIcecanditate called from callviewmodel")
//                    viewModelScope.launch {
//                        socketRepository.sendIceCandidate(userName, targetUserName, iceCandidate!!)
//                    }
//                }
//
//                override fun onAddStream(mediaStream: MediaStream?) {
//                    super.onAddStream(mediaStream)
//                    mediaStream?.videoTracks?.get(0)?.addSink(remoteRender)
//                    Log.d(TAG, "onAddStream call")
//                }
//            })
        }
    }


//    fun setLocalRenderView(localRenderer: SurfaceViewRenderer) {
//        localRender = localRenderer
//        rtcClient.initSurfaceView(localRender)
//        rtcClient.startLocalVideo(localRender)
//    }
//
//    fun setRemoteRenderView(remoteRenderer: SurfaceViewRenderer) {
//        remoteRender = remoteRenderer
//        rtcClient.initSurfaceView(remoteRender)
//    }
//
//    private suspend fun observeResponse() {
//        socketRepository.receiveResponse().onEach { response ->
//            when (response.type) {
//                ResponseType.ICE_CANDIDATE_RESPONSE -> {
//                    val iceResponsedata = gson.fromJson(response.data, IceResponseData::class.java)
//                    try {
//                        rtcClient.addIceCandidate(
//                            IceCandidate(
//                                iceResponsedata.sdpMid,
//                                Math.toIntExact(iceResponsedata.sdpMLineIndex.toLong()),
//                                iceResponsedata.sdpCandidate
//                            )
//                        )
//                        Log.e(TAG, "ice response: ${iceResponsedata.sdpCandidate}")
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                }
//
//                else -> {}
//            }
//        }.launchIn(viewModelScope)
//    }
//}
