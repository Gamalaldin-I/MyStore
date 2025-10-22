package com.example.htopstore.di.module

import com.example.data.local.sharedPrefs.SharedPref
import com.example.data.remote.RemoteExpensesRepo
import com.example.data.remote.RemoteProductRepo
import com.example.data.remote.RemoteSalesRepo
import com.example.htopstore.util.firebase.RemoteExpensesRepoImp
import com.example.htopstore.util.firebase.RemoteProductRepoImp
import com.example.htopstore.util.firebase.RemoteSalesRepoImp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
    fun provideRemoteProductRepo(db: FirebaseFirestore,sharedPref: SharedPref): RemoteProductRepo {
        return RemoteProductRepoImp(db,sharedPref)
    }
    @Provides
    fun provideRemoteSalesRepo(db: FirebaseFirestore): RemoteSalesRepo{
        return RemoteSalesRepoImp(db)
    }
    @Provides
    fun provideExpensesRepo(db: FirebaseFirestore): RemoteExpensesRepo{
        return RemoteExpensesRepoImp(db)
    }
}