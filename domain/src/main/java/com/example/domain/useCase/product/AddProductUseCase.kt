package com.example.domain.useCase.product

import com.example.domain.model.Product
import com.example.domain.repo.ProductRepo

class AddProductUseCase(private val productRepo: ProductRepo){
    suspend operator fun invoke(product: Product){
        productRepo.addProduct(product)
    }
}