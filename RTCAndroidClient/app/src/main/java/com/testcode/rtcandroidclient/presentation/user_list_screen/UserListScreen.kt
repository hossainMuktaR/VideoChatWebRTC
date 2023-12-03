package com.testcode.rtcandroidclient.presentation.user_list_screen

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.testcode.rtcandroidclient.common.Constant
import com.testcode.rtcandroidclient.presentation.Screen
import com.testcode.rtcandroidclient.presentation.call_screen.CallScreen
import com.testcode.rtcandroidclient.presentation.login_screen.LoginSideEffect
import kotlinx.coroutines.flow.collectLatest

@Composable
fun UserListScreen(
    navController: NavController,
    vm: UserlistViewModel = hiltViewModel()
) {
    BackHandler(true) {
        vm.closeSocketConnection()
        navController.popBackStack()
    }
    val state = vm.state.value
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        vm.sideEffect.collectLatest { sideEffect ->
            when (sideEffect) {
                is UserListSideEffect.GoCallScreen -> {
                    Log.e("UserlistScreen","going to call screen")
                    navController.popBackStack()
                    navController.navigate(
                        Screen.CallScreen.route +
                                "?${Constant.USERNAMEKEY}=${sideEffect.userName}" +
                                "&${Constant.TARGETNAMEKEY}=${sideEffect.targetName}")
                }
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopStart
    ) {

        if(state.isCallRequest) {
            AlertDialog(
                onDismissRequest = {
                    vm.callReject()
                },
                confirmButton = {
                    Button(onClick = {
                        state.callerName?.let { targetName ->
                            vm.answer(targetName)
                        }
                    }) {
                        Text("Receive")
                    }
                },
                title = { Text("Incoming Call") },
                text = { Text(text = "Your have call from ${state.callerName}") }
            )
        }else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                contentAlignment = Alignment.TopStart
            ) {
                LazyColumn() {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = state.userName!!)
                            Button(onClick = {
                                vm.requestGetOnlineUser(state.userName)
                            }) {
                                Text("Refresh List")
                            }
                        }
                    }
                    if (state.listOfMember.isEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "0 Online User")
                            }
                        }
                    } else {
                        items(state.listOfMember) { userName ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = userName)
                                IconButton(onClick = {
                                    vm.call(userName)
                                }) {
                                    Icon(imageVector = Icons.Default.Call, contentDescription = null)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}