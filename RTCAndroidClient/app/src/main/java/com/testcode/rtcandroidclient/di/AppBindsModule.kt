package com.testcode.rtcandroidclient.di

import com.testcode.rtcandroidclient.data.repository.SocketRepository
import com.testcode.rtcandroidclient.data.repository.SocketRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindsModule {
    @Binds
    abstract fun getSocketRepository(socketRepositoryImpl: SocketRepositoryImpl): SocketRepository
}