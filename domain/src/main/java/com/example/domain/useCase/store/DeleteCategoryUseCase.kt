package com.example.domain.useCase.store

import com.example.domain.repo.StoreRepo

class DeleteCategoryUseCase(private val repo: StoreRepo) {
    suspend operator fun invoke(category: String): Pair<Boolean, String> {
        return repo.deleteCategory(category)
    }
}