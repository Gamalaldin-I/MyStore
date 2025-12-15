package com.example.domain.useCase.product

import com.example.domain.model.Product
import com.example.domain.repo.ProductRepo
import kotlinx.coroutines.flow.Flow

class GetPendingProductsYUseCase(private val repo: ProductRepo){
    operator fun invoke():Flow<List<Product>> = repo.getPendingProducts()
}