package com.example.domain.useCase.product

import com.example.domain.repo.ProductRepo
import com.example.domain.repo.StaffRepo

class DeleteProductUseCase(private val productRepo: ProductRepo,private val staffRepo: StaffRepo) {
    suspend operator fun invoke(id: String, image: String): Pair<Boolean, String> {
        /////////////////////////////before operation/////////////////////////////////
        val resOfGo = staffRepo.preformAction()
        if(!resOfGo.first){
            return Pair(false,resOfGo.second)
        }
        return productRepo.deleteProductById(id, image)
    }
}