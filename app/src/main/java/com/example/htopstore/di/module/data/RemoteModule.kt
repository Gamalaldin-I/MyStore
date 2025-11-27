package com.example.htopstore.di.module.data

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteModule {
    @Provides
    @Singleton
    fun provideSupabase(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = "https://ayoanqjzciolnahljauc.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImF5b2FucWp6Y2lvbG5haGxqYXVjIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE4MjkyMTEsImV4cCI6MjA3NzQwNTIxMX0.m3GnKY2PfmspT4-Ek43_YAkV1LUWMTgPWcZuO5wFbPU"
        ) {
            install(Postgrest.Companion)
            install(Storage.Companion)
            install(Realtime.Companion)
            install(Auth.Companion)
        }
    }
}