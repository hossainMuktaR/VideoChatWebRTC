package com.testcode.rtcandroidclient.presentation.login_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.testcode.rtcandroidclient.common.Constant
import com.testcode.rtcandroidclient.presentation.Screen
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    vm: LoginViewModel = hiltViewModel()
) {
    LaunchedEffect(key1 = true) {
        vm.sideEffect.collectLatest { sideEffect ->
            when (sideEffect) {
                is LoginSideEffect.LoginSuccess -> {
                    navController.navigate(Screen.UserListScreen.route + "?${Constant.USERNAMEKEY}=${sideEffect.userName}")
                }
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(value = vm.userName.value, onValueChange = vm::changeUserName)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = vm.isEmulator.value, onCheckedChange = vm::changeDevice)
            Text("is Emulator")
        }
        Button(onClick = vm::login) {
            Text("Login")
        }
    }
}