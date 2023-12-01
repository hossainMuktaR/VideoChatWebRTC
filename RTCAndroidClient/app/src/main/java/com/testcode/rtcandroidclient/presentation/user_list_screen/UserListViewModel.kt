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
import com.testcode.rtcandroidclient.data.remote.CallResponseData
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

    private val gson = Gson()
    private val _state = mutableStateOf(UserListState())
    val state: State<UserListState> = _state

    private val _sideEffect = MutableSharedFlow<UserListSideEffect>()
    val sideEffect: SharedFlow<UserListSideEffect> = _sideEffect


    init {
        _state.value = state.value.copy(
            userName = savedStateHandle.get<String>(Constant.USERNAMEKEY) ?: "Guest"
        )
        Log.e(TAG, "Your UserName: ${state.value.userName}")
        viewModelScope.launch {
            observeResponse()
            requestGetOnlineUser(state.value.userName!!)
        }
    }

    fun requestGetOnlineUser(userName: String) {
        viewModelScope.launch {
            socketRepository.getOnlineUser(userName)
            println("online user request send")
        }
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

                ResponseType.CALL_REQUEST_RESPONSE -> {
                    val callResponseData = gson.fromJson(response.data, CallResponseData::class.java)
                    callResponseData.target?.let{
                        _state.value = _state.value.copy(
                            callerName = it,
                            isCallRequest = true
                        )
                    }
                }
                else -> {}
            }

        }.launchIn(viewModelScope)
    }

    fun call(target: String) {
        _state.value = state.value.copy(
            callerName = target,
        )
        val userName = _state.value.userName!!
        viewModelScope.launch {
            socketRepository.sendCallRequest(userName, target)
            _sideEffect.emit(UserListSideEffect.GoCallScreen(userName, target))
        }
        Log.d(TAG,"Call request send")
    }

    fun answer(target: String) {
        viewModelScope.launch {
            _sideEffect.emit(UserListSideEffect.GoCallScreen(state.value.userName!!, target))
        }
    }
    fun callReject() {
        _state.value = state.value.copy(
            isCallRequest = false
        )
    }
}

sealed class UserListSideEffect {
    class GoCallScreen(val userName: String, val targetName: String) : UserListSideEffect()
}

