package com.example.domain.useCase.product

import com.example.domain.repo.ProductRepo

class DeleteProductUseCase(private val productRepo: ProductRepo) {
    suspend operator fun invoke(id: String, image: String): Pair<Boolean, String> {
        return productRepo.deleteProductById(id, image)
    }
}