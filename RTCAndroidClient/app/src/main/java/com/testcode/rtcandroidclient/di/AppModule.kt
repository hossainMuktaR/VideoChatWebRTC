package com.testcode.rtcandroidclient.di

import android.app.Application
import com.testcode.rtcandroidclient.data.rtc.RtcClient
import com.testcode.rtcandroidclient.data.remote.SocketClient
import com.testcode.rtcandroidclient.data.repository.SocketRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(CIO){
            install(WebSockets) {
                maxFrameSize = Long.MAX_VALUE
            }

        }
    }
    @Provides
    @Singleton
    fun provideSocketClient(client: HttpClient): SocketClient {
        return SocketClient(client)
    }

//    @Provides
//    @Singleton
//    fun provideRtcClient(app: Application, socketRepository: SocketRepository): RtcClient {
//        return RtcClient(app, socketRepository)
//    }

}