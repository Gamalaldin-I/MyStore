package com.example.domain.useCase.product

import com.example.domain.repo.ProductRepo
import kotlinx.coroutines.flow.Flow

class GetArchiveSizeUseCase(private val productRepo: ProductRepo){
     operator fun invoke(): Flow<Int>{
        return productRepo.getArchiveLength()
    }
}