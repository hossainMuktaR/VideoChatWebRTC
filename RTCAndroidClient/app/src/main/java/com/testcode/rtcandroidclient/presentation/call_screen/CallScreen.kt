package com.testcode.rtcandroidclient.presentation.call_screen

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import org.webrtc.SurfaceViewRenderer

private val TAG = "CallScreen"

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallScreen(
    navController: NavController,
    vm: CallViewModel = hiltViewModel()
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomCallController(items = vm.cBtnList, onEndClick = {
                vm.callEnd()
                navController.popBackStack()
            })
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(5.dp))
                .padding(it),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { context ->
                    SurfaceViewRenderer(context).also {
                        vm.setRemoteRenderView(it)
                    }
                },
            )
            Box(
                modifier = Modifier
                    .size(height = 200.dp, width = 112.5.dp)
                    .padding(2.dp)
                    .align(Alignment.TopEnd)
            ) {
                AndroidView(
                    factory = { context ->
                        SurfaceViewRenderer(context).also {
                            vm.setLocalRenderView(it)
                        }
                    },
                    modifier = Modifier.clip(RoundedCornerShape(50.dp))
                )
            }

        }
    }
}