package com.testcode.rtcandroidclient.presentation.login_screen

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.testcode.rtcandroidclient.data.repository.SocketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val socketRepository: SocketRepository
) : ViewModel() {
    private val _userName = mutableStateOf("")
    val userName: State<String> = _userName

    private val _isEmulator = mutableStateOf<Boolean>(false)
    val isEmulator: State<Boolean> = _isEmulator

    private val _sideEffect = MutableSharedFlow<LoginSideEffect>()
    val sideEffect: SharedFlow<LoginSideEffect> = _sideEffect
    

    fun changeUserName(value: String) {
        _userName.value = value
    }
    fun changeDevice(value: Boolean) {
        _isEmulator.value = value
    }

    fun login() {
        if (_userName.value.isNotBlank()) {
            viewModelScope.launch {
                socketRepository.initSocket(_isEmulator.value)
                socketRepository.storeUser(_userName.value)
                println("request Send")
            _sideEffect.emit(LoginSideEffect.LoginSuccess(_userName.value))
            }
        }else {
            println("username blank")
        }
    }
}

sealed class LoginSideEffect {
    class LoginSuccess(val userName: String) : LoginSideEffect()
}
