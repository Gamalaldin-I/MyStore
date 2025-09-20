package com.example.domain.useCase.product

import com.example.domain.model.Product
import com.example.domain.repo.ProductRepo

class GetLowStockUseCase(private val productRepo: ProductRepo){
    suspend operator fun invoke(): List<Product> {
        return productRepo.getLowStock()
    }
}