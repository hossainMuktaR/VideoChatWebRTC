package com.testcode.rtcandroidclient.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.testcode.rtcandroidclient.common.Constant
import com.testcode.rtcandroidclient.presentation.call_screen.CallScreen
import com.testcode.rtcandroidclient.presentation.login_screen.LoginScreen
import com.testcode.rtcandroidclient.presentation.user_list_screen.UserListScreen
import com.testcode.rtcandroidclient.ui.theme.RTCAndroidClientTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RTCAndroidClientTheme {
                Surface {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Screen.LoginScreen.route
                    ) {
                        composable(Screen.LoginScreen.route) {
                            LoginScreen(navController = navController)
                        }
                        composable(
                            route = Screen.UserListScreen.route +
                            "?${Constant.USERNAMEKEY}={${Constant.USERNAMEKEY}}",
                            arguments = listOf(
                                navArgument(
                                    name = Constant.USERNAMEKEY
                                ){
                                    type = NavType.StringType
                                    defaultValue = "Guest"
                                }
                            )
                        ){
                            UserListScreen(navController = navController)
                        }
                        composable(
                            route = Screen.CallScreen.route +
                            "?${Constant.USERNAMEKEY}={${Constant.USERNAMEKEY}}" +
                                    "&${Constant.TARGETNAMEKEY}={${Constant.TARGETNAMEKEY}}",
                            arguments = listOf(
                                navArgument(
                                    name = Constant.USERNAMEKEY
                                ){
                                    type = NavType.StringType
                                    defaultValue = "Guest"
                                },
                                navArgument(
                                    name = Constant.TARGETNAMEKEY
                                ){
                                    type = NavType.StringType
                                    defaultValue = ""
                                }
                            )
                        ) {
//                            CallScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}
