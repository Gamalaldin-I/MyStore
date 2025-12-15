package com.example.domain.useCase.product

import com.example.domain.repo.ProductRepo

class DeletePendingProductUseCase(private val repo: ProductRepo) {
    suspend operator fun invoke(id: String): Boolean {
        return repo.deletePendingProduct(id)
    }
}