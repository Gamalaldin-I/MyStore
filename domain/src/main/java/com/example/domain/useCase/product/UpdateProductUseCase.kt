package com.example.domain.useCase.product

import com.example.domain.model.Product
import com.example.domain.repo.ProductRepo

class UpdateProductUseCase(private val productRepo: ProductRepo) {
    suspend operator fun invoke(product: Product): Pair<Boolean, String> {
       return productRepo.updateProduct(product )
    }
}