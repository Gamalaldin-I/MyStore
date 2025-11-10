package com.example.domain.repo

import com.example.domain.model.Store

interface StoreRepo {
    suspend fun createStore(store:Store): Pair<Boolean, String>
    suspend fun updateStore(store: Store): Pair<Boolean, String>
    fun deleteStore(id:String)
    fun getStore(id: String)
}