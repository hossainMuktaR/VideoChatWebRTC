package com.testcode.rtcandroidclient.data.rtc

import androidx.lifecycle.viewModelScope
import com.testcode.rtcandroidclient.data.repository.SocketRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.webrtc.IceCandidate
import org.webrtc.MediaStream

class PeerObserverImpl(
    private val rtcClient: RtcClient,
    val socketRepository: SocketRepository,
): PeerConnectionObserver() {
    override fun onIceCandidate(p0: IceCandidate?) {
        super.onIceCandidate(p0)
        rtcClient.addIceCandidate(p0)
        println("onicecanditate called from callviewmodel")
        runBlocking {
            withContext(Dispatchers.IO){
//                socketRepository.sendIceCandidate(userName, targetUserName, iceCandidate!!)
            }
        }
    }

    override fun onAddStream(p0: MediaStream?) {
        super.onAddStream(p0)
    }
}