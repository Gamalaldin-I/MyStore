package com.example.htopstore.di.module

import android.content.Context
import com.example.data.remote.NetworkHelperInterface
import com.example.htopstore.util.NetworkHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideNetworkHelperInterface(@ApplicationContext context: Context): NetworkHelperInterface{
        return NetworkHelper(context)
    }
}