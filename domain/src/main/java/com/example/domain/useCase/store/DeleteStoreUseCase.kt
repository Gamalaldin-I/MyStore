package com.example.domain.useCase.store

import com.example.domain.repo.StoreRepo

class DeleteStoreUseCase (private val repo: StoreRepo){
    suspend operator fun invoke(id:String):Pair<Boolean,String>{
        return repo.deleteStore(id)
    }
}