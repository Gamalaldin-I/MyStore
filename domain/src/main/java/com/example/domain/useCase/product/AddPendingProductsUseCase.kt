package com.example.domain.useCase.product

import com.example.domain.model.Product
import com.example.domain.repo.ProductRepo

class AddPendingProductsUseCase(private val repo: ProductRepo) {
    suspend operator fun invoke(listOfPending: List<Product>,
                                onProgress:(Int)->Unit,
                                onFinish:()->Unit){
        return repo.addPendingProducts(listOfPending,onProgress,onFinish)
    }
}