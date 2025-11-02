package com.example.htopstore.di.module

import android.content.Context
import com.example.data.local.sharedPrefs.SharedPref
import com.example.data.remote.repo.RemoteProductRepo
import com.example.data.remote.supabase.SupabaseHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteModule {
    @Provides
    @Singleton
    fun provideFireBaseFireStore(): FirebaseFirestore{
        return Firebase.firestore
    }
    @Provides
    @Singleton
    fun provideSupabase(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = "https://ayoanqjzciolnahljauc.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImF5b2FucWp6Y2lvbG5haGxqYXVjIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE4MjkyMTEsImV4cCI6MjA3NzQwNTIxMX0.m3GnKY2PfmspT4-Ek43_YAkV1LUWMTgPWcZuO5wFbPU"
        ) {
            install(Postgrest)
            install(Storage)
        }
    }
    @Provides
    @Singleton
    fun provideSupabaseHelper(supabase: SupabaseClient,@ApplicationContext context: Context): SupabaseHelper {
        return SupabaseHelper(supabase,context)
    }
    @Provides
    fun provideRemoteProductRepo(firestore: FirebaseFirestore,
                                 supabase: SupabaseHelper,
                                 pref: SharedPref,
                                 ): RemoteProductRepo {
        return RemoteProductRepo(firestore,supabase,pref)
    }

}