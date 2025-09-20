package com.example.domain.useCase.product

import com.example.domain.model.Product
import com.example.domain.repo.ProductRepo

class GetTop5UseCase(private val productRepo: ProductRepo) {
    suspend operator fun invoke(): List<Product> {
        return productRepo.getTop5InSales()
    }
}