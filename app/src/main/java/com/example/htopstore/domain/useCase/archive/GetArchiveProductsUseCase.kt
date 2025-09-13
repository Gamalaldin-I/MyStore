package com.example.htopstore.domain.useCase.archive

import com.example.htopstore.data.local.model.Product
import com.example.htopstore.data.local.repo.archieve.ArchiveRepoImp
import javax.inject.Inject

class GetArchiveProductsUseCase @Inject constructor(private val productRepository: ArchiveRepoImp) {
    suspend operator fun invoke(): List<Product> {
        return productRepository.getArchiveProducts()
    }
}