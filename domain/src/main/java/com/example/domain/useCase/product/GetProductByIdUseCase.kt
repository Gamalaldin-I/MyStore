package com.example.domain.useCase.product

import com.example.domain.model.Product
import com.example.domain.repo.ProductRepo

class GetProductByIdUseCase(private val repo: ProductRepo) {
    suspend operator fun invoke(id: String): Product? {
        return repo.getProductById(id)
    }

}