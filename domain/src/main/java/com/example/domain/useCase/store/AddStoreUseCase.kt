package com.example.domain.useCase.store

import com.example.domain.model.Store
import com.example.domain.repo.StoreRepo

class AddStoreUseCase(private val repo: StoreRepo){
    suspend operator fun invoke(store: Store): Pair<Boolean, String> {
        return repo.createStore(store)
    }
}