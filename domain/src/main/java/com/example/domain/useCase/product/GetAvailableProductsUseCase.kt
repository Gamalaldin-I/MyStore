package com.example.domain.useCase.product

import com.example.domain.model.Product
import com.example.domain.repo.ProductRepo
import kotlinx.coroutines.flow.Flow

class GetAvailableProductsUseCase(private val productRepo: ProductRepo) {
     operator fun invoke(): Flow<List<Product>> {
        return productRepo.getAvailableProducts()
    }
}