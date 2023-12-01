package com.testcode.rtcandroidclient.data.rtc

import android.app.Application
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import com.testcode.rtcandroidclient.data.repository.SocketRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.webrtc.Camera2Enumerator
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnection.Observer
import org.webrtc.PeerConnectionFactory
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoCapturer


class RtcClient(
    private val application: Application,
    private val socketRepository: SocketRepository
) {
    private lateinit var scope: CoroutineScope
    private lateinit var observer: Observer

    init {
        initPeerConnectionFactory(application)
    }

    fun setScope(scope: CoroutineScope) {
        this.scope = scope
    }
    fun setObserver(observer: Observer) {
        this.observer = observer
    }

    private val eglBase: EglBase = EglBase.create()
    private val peerConnectionFactory by lazy { createPeerConnectionFactory() }
    private val iceServer = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
            .createIceServer()
    )
    private val mediaConstraints = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
    }

    private val peerConnection by lazy {   createPeerConnection(observer) }
    private val localVideoSource by lazy { peerConnectionFactory.createVideoSource(false) }
    private val localAudioSource by lazy { peerConnectionFactory.createAudioSource(MediaConstraints()) }
    private fun initPeerConnectionFactory(application: Application) {
        val peerConnectionOptions = PeerConnectionFactory.InitializationOptions.builder(application)
            .setEnableInternalTracer(true)
            .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
            .createInitializationOptions()
        PeerConnectionFactory.initialize(peerConnectionOptions)
    }

    private fun createPeerConnectionFactory(): PeerConnectionFactory {
        return PeerConnectionFactory.builder()
            .setVideoEncoderFactory(
                DefaultVideoEncoderFactory(
                    eglBase.eglBaseContext,
                    true,
                    true
                )
            )
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase.eglBaseContext))
            .setOptions(
                PeerConnectionFactory.Options().apply {
                    disableEncryption = true
                    disableNetworkMonitor = true
                }
            ).createPeerConnectionFactory()
    }

    private fun createPeerConnection(observer: Observer): PeerConnection? {
        return peerConnectionFactory.createPeerConnection(iceServer, observer)
    }

    fun initSurfaceView(surface: SurfaceViewRenderer) {
        surface.run {
            setEnableHardwareScaler(true)
            setMirror(true)
            init(eglBase.eglBaseContext, null)
        }
    }

    fun startLocalVideo(surface: SurfaceViewRenderer) {
        // Use the main looper for WebRTC operations
        val rtcThread = HandlerThread("RTCThread")
        rtcThread.start()
        val surfaceTextureHelper =
            SurfaceTextureHelper.create(rtcThread.name, eglBase.eglBaseContext)
        val videoCapturer = getVideoCapturer(application)
        videoCapturer.initialize(
            surfaceTextureHelper,
            surface.context,
            localVideoSource.capturerObserver
        )
        videoCapturer.startCapture(320, 240, 30)
        val localVideoTrack =
            peerConnectionFactory.createVideoTrack("local_track_video", localVideoSource)
        localVideoTrack?.addSink(surface)
        val localAudioTrack =
            peerConnectionFactory.createAudioTrack("local_track_audio", localAudioSource)
        val localStream = peerConnectionFactory.createLocalMediaStream("local_stream")
        localStream.addTrack(localVideoTrack)
        localStream.addTrack(localAudioTrack)
        peerConnection?.addStream(localStream)
        Log.d("RtcClient", "Local video and audio streams added to PeerConnection")
    }
    //refactor code
//    fun startLocalVideo(surface: SurfaceViewRenderer) {
//        checkNotNull(peerConnectionFactory) { "PeerConnectionFactory must not be null" }
//        checkNotNull(localVideoSource) { "LocalVideoSource must not be null" }
//        checkNotNull(peerConnection) { "PeerConnection must not be null" }
//
//        // Use the main looper for WebRTC operations
//        val rtcThread = HandlerThread("RTCThread")
//        rtcThread.start()
//
//        val surfaceTextureHelper =
//            SurfaceTextureHelper.create("RTCThread", eglBase.eglBaseContext)
//
//        // Ensure videoCapturer is not null and properly initialized
//        val videoCapturer = getVideoCapturer(application)
//        checkNotNull(videoCapturer) { "VideoCapturer must not be null" }
//
//        videoCapturer.initialize(surfaceTextureHelper, surface.context, localVideoSource.capturerObserver)
//        videoCapturer.startCapture(320, 240, 30) // Adjusted resolution to 720p
//
//        // Create local video track
//        val localVideoTrack = peerConnectionFactory.createVideoTrack("local_track_video", localVideoSource)
//        checkNotNull(localVideoTrack) { "LocalVideoTrack must not be null" }
//
//        // Add the local video track to the SurfaceViewRenderer
//        localVideoTrack.addSink(surface)
//
//        // Create local audio track
//        val localAudioTrack = peerConnectionFactory.createAudioTrack("local_track_audio", localAudioSource)
//        checkNotNull(localAudioTrack) { "LocalAudioTrack must not be null" }
//
//        // Create and add local media stream
//        val localStream = peerConnectionFactory.createLocalMediaStream("local_stream")
//        localStream.addTrack(localVideoTrack)
//        localStream.addTrack(localAudioTrack)
//
//        // Add the local media stream to the PeerConnection
//        peerConnection.addStream(localStream)
//
//        Log.d("RtcClient", "Local video and audio streams added to PeerConnection")
//    }


    private fun getVideoCapturer(application: Application): VideoCapturer {
        return Camera2Enumerator(application).run {
            deviceNames.find {
                isFrontFacing(it)
            }?.let {
                createCapturer(it, null)
            } ?: throw IllegalStateException()
        }
    }

    fun call(userName: String, targetName: String) {

        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sDes: SessionDescription?) {
                peerConnection?.setLocalDescription(object : SdpObserver {
                    override fun onCreateSuccess(p0: SessionDescription?) {
                        TODO("Not yet implemented")
                    }

                    override fun onSetSuccess() {
                        val offer = hashMapOf(
                            "sdp" to sDes?.description,
                            "type" to sDes?.type
                        )
                        sDes?.description?.let { sdp ->
                            scope.launch {
                                socketRepository.sendOffer(userName, targetName, sDes.description)
                            }
                            Log.e("RtcClient", "call offer sdp: ${sDes.description}")
                        }
                    }

                    override fun onCreateFailure(p0: String?) {
                        TODO("Not yet implemented")
                    }

                    override fun onSetFailure(p0: String?) {
                        TODO("Not yet implemented")
                    }
                }, sDes)
            }

            override fun onSetSuccess() {
                TODO("Not yet implemented")
            }

            override fun onCreateFailure(p0: String?) {
                TODO("Not yet implemented")
            }

            override fun onSetFailure(p0: String?) {
                TODO("Not yet implemented")
            }

        }, mediaConstraints)
    }

    fun onRemoteSessionReceived(session: SessionDescription) {
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {
                TODO("Not yet implemented")
            }

            override fun onSetSuccess() {
            }

            override fun onCreateFailure(p0: String?) {
                TODO("Not yet implemented")
            }

            override fun onSetFailure(p0: String?) {
                TODO("Not yet implemented")
            }
        }, session)
    }

    fun answer(userName: String, target: String) {
        peerConnection?.createAnswer(
            object : SdpObserver {
                override fun onCreateSuccess(sDes: SessionDescription?) {
                    peerConnection?.setLocalDescription(
                        object : SdpObserver {
                            override fun onCreateSuccess(p0: SessionDescription?) {
                                TODO("Not yet implemented")
                            }

                            override fun onSetSuccess() {
                                scope.launch {
                                    sDes?.description?.let { sdp ->
                                        socketRepository.sendCreateAnswer(userName, target, sdp)
                                        Log.e("RtcClient", "call answer sdp: ${sDes.description}")
                                    }
                                }
                            }

                            override fun onCreateFailure(p0: String?) {
                                TODO("Not yet implemented")
                            }

                            override fun onSetFailure(p0: String?) {
                                TODO("Not yet implemented")
                            }
                        }, sDes
                    )
                }

                override fun onSetSuccess() {
                    TODO("Not yet implemented")
                }

                override fun onCreateFailure(p0: String?) {
                    TODO("Not yet implemented")
                }

                override fun onSetFailure(p0: String?) {
                    TODO("Not yet implemented")
                }
            }, mediaConstraints
        )
    }

    fun addIceCandidate(ice: IceCandidate?) {
        if(peerConnection != null) {
            peerConnection?.addIceCandidate(ice)
            Log.d("RtcClient", "Ice Candidate Added")
        }else {
            Log.d("RtcClient", "peerConnection is null")
        }

    }

    fun endCall() {
        peerConnection?.close()
    }


}