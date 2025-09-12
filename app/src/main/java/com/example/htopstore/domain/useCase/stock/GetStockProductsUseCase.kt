package com.example.htopstore.domain.useCase.stock

import com.example.htopstore.data.local.model.Product
import com.example.htopstore.data.local.repo.stock.StockRepoImp
import javax.inject.Inject

class GetStockProductsUseCase @Inject constructor(private val stockRepo : StockRepoImp){
    suspend operator fun invoke(): List<Product>{
        return stockRepo.getAvailableProducts()
    }
}