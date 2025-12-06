package com.example.domain.useCase.product

import com.example.domain.model.Product
import com.example.domain.repo.ProductRepo
import com.example.domain.repo.StaffRepo

class AddProductUseCase(private val productRepo: ProductRepo,private val staffRepo: StaffRepo){
    suspend operator fun invoke(product: Product):Pair<Boolean,String>{
        /////////////////////////////before operation/////////////////////////////////
        val resOfGo = staffRepo.preformAction()
        if(!resOfGo.first){
            return Pair(false,resOfGo.second)
        }
        return productRepo.addProduct(product )
    }
}