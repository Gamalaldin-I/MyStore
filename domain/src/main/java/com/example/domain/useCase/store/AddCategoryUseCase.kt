package com.example.domain.useCase.store

import com.example.domain.repo.StoreRepo

class AddCategoryUseCase(private val repo: StoreRepo) {
    suspend operator fun invoke(category: String): Pair<Boolean, String> {
        return repo.addCategory(category)
    }
}